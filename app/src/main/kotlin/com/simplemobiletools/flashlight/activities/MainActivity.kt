package com.simplemobiletools.flashlight.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_EVENT_BUS
import com.simplemobiletools.commons.helpers.PERMISSION_CAMERA
import com.simplemobiletools.commons.helpers.isNougatMR1Plus
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.databinding.ActivityMainBinding
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.CameraTorchListener
import com.simplemobiletools.flashlight.helpers.MIN_BRIGHTNESS_LEVEL
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class MainActivity : SimpleActivity() {
    companion object {
        private const val MAX_STROBO_DELAY = 2000L
        private const val MIN_STROBO_DELAY = 10L
        private const val FLASHLIGHT_STATE = "flashlight_state"
        private const val STROBOSCOPE_STATE = "stroboscope_state"
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private var mBus: EventBus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var reTurnFlashlightOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()

        mBus = EventBus.getDefault()

        binding.apply {
            updateMaterialActivityViews(mainCoordinator, mainHolder, useTransparentNavigation = true, useTopSearchMenu = false)
            setupMaterialScrollListener(mainNestedScrollview, mainToolbar)

            changeIconColor(getContrastColor(), stroboscopeBtn)

            brightDisplayBtn.setOnClickListener {
                reTurnFlashlightOn = false
                startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
            }

            flashlightBtn.setOnClickListener {
                mCameraImpl!!.toggleFlashlight()
            }

            sosBtn.setOnClickListener {
                toggleStroboscope(true)
            }

            stroboscopeBtn.setOnClickListener {
                toggleStroboscope(false)
            }
        }

        setupStroboscope()
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.mainToolbar)
        mCameraImpl!!.handleCameraSetup()
        checkState(MyCameraImpl.isFlashlightOn)

        val contrastColor = getContrastColor()

        binding.apply {
            changeIconColor(contrastColor, brightDisplayBtn)
            brightDisplayBtn.beVisibleIf(config.brightDisplay)
            sosBtn.beVisibleIf(config.sos)

            if (sosBtn.currentTextColor != getProperPrimaryColor()) {
                sosBtn.setTextColor(contrastColor)
            }

            stroboscopeBtn.beVisibleIf(config.stroboscope)

            if (!config.stroboscope) {
                mCameraImpl!!.stopStroboscope()
                stroboscopeBar.beInvisible()
            }

            updateTextColors(mainHolder)
            if (stroboscopeBar.isInvisible()) {
                changeIconColor(contrastColor, stroboscopeBtn)
            }
        }

        requestedOrientation = if (config.forcePortraitMode) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR
        invalidateOptionsMenu()

        if (config.turnFlashlightOn && reTurnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }

        reTurnFlashlightOn = true

        checkShortcuts()
    }

    override fun onStart() {
        super.onStart()
        mBus!!.register(this)

        if (mCameraImpl == null) {
            setupCameraImpl()
        }
    }

    override fun onStop() {
        super.onStop()
        mBus!!.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    private fun setupOptionsMenu() {
        binding.mainToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun refreshMenuItems() {
        binding.mainToolbar.menu.apply {
            findItem(R.id.more_apps_from_us).isVisible = !resources.getBoolean(R.bool.hide_google_relations)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FLASHLIGHT_STATE, mIsFlashlightOn)
        outState.putBoolean(STROBOSCOPE_STATE, binding.stroboscopeBar.isVisible())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val isFlashlightOn = savedInstanceState.getBoolean(FLASHLIGHT_STATE, false)
        if (isFlashlightOn) {
            mCameraImpl!!.toggleFlashlight()
        }

        val isStroboscopeOn = savedInstanceState.getBoolean(STROBOSCOPE_STATE, false)
        if (isStroboscopeOn) {
            toggleStroboscope(false)
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        reTurnFlashlightOn = false
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        reTurnFlashlightOn = false
        val licenses = LICENSE_EVENT_BUS

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
            FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this, object : CameraTorchListener {
            override fun onTorchEnabled(isEnabled: Boolean) {
                mCameraImpl!!.onTorchEnabled(isEnabled)
                if (mCameraImpl!!.supportsBrightnessControl()) {
                    binding.brightnessBar.beVisibleIf(isEnabled)
                }
            }

            override fun onTorchUnavailable() {
                mCameraImpl!!.onCameraNotAvailable()
            }
        })
        if (config.turnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }
        setupBrightness()
    }

    private fun setupStroboscope() {
        binding.stroboscopeBar.apply {
            max = (MAX_STROBO_DELAY - MIN_STROBO_DELAY).toInt()
            progress = config.stroboscopeProgress
            onSeekBarChangeListener { progress ->
                val frequency = max - progress + MIN_STROBO_DELAY
                mCameraImpl?.stroboFrequency = frequency
                config.stroboscopeFrequency = frequency
                config.stroboscopeProgress = progress
            }
        }
    }

    private fun toggleStroboscope(isSOS: Boolean) {
        // use the old Camera API for stroboscope, the new Camera Manager is way too slow
        if (isNougatPlus()) {
            cameraPermissionGranted(isSOS)
        } else {
            handlePermission(PERMISSION_CAMERA) {
                if (it) {
                    cameraPermissionGranted(isSOS)
                } else {
                    toast(R.string.camera_permission)
                }
            }
        }
    }

    private fun setupBrightness() {
        binding.brightnessBar.apply {
            max = mCameraImpl?.getMaximumBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
            progress = mCameraImpl?.getCurrentBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
            onSeekBarChangeListener { level ->
                val newLevel = level.coerceAtLeast(MIN_BRIGHTNESS_LEVEL)
                mCameraImpl?.updateBrightnessLevel(newLevel)
                config.brightnessLevel = newLevel
            }
        }
    }

    private fun cameraPermissionGranted(isSOS: Boolean) {
        if (isSOS) {
            val isSOSRunning = mCameraImpl!!.toggleSOS()
            binding.sosBtn.setTextColor(if (isSOSRunning) getProperPrimaryColor() else getContrastColor())
        } else if (mCameraImpl!!.toggleStroboscope()) {
            binding.apply {
                stroboscopeBar.beInvisibleIf(stroboscopeBar.isVisible())
                changeIconColor(if (stroboscopeBar.isVisible()) getProperPrimaryColor() else getContrastColor(), stroboscopeBtn)
            }
        }
    }

    private fun getContrastColor() = getProperBackgroundColor().getContrastColor()

    private fun releaseCamera() {
        mCameraImpl?.releaseCamera()
        mCameraImpl = null
    }

    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        checkState(event.isEnabled)
    }

    @Subscribe
    fun stopStroboscope(event: Events.StopStroboscope) {
        binding.stroboscopeBar.beInvisible()
        changeIconColor(getContrastColor(), binding.stroboscopeBtn)
    }

    @Subscribe
    fun stopSOS(event: Events.StopSOS) {
        binding.sosBtn.setTextColor(getContrastColor())
    }

    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    private fun enableFlashlight() {
        changeIconColor(getProperPrimaryColor(), binding.flashlightBtn)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = true

        binding.apply {
            sosBtn.setTextColor(getContrastColor())

            changeIconColor(getContrastColor(), stroboscopeBtn)
            stroboscopeBar.beInvisible()
        }
    }

    private fun disableFlashlight() {
        changeIconColor(getContrastColor(), binding.flashlightBtn)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = false
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.applyColorFilter(color)
    }

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = config.appIconColor
        if (isNougatMR1Plus() && config.lastHandledShortcutColor != appIconColor) {
            val createNewContact = getBrightDisplayShortcut(appIconColor)

            try {
                shortcutManager.dynamicShortcuts = Arrays.asList(createNewContact)
                config.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getBrightDisplayShortcut(appIconColor: Int): ShortcutInfo {
        val brightDisplay = getString(R.string.bright_display)
        val drawable = resources.getDrawable(R.drawable.shortcut_bright_display)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_bright_display_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, BrightDisplayActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(this, "bright_display")
            .setShortLabel(brightDisplay)
            .setLongLabel(brightDisplay)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    @Subscribe
    fun cameraUnavailable(event: Events.CameraUnavailable) {
        toast(R.string.camera_error)
        disableFlashlight()
    }
}
