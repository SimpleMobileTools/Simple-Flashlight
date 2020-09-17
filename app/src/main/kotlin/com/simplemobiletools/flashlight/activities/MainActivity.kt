package com.simplemobiletools.flashlight.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.simplemobiletools.flashlight.models.Events
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class MainActivity : SimpleActivity() {
    private val MAX_STROBO_DELAY = 2000L
    private val MIN_STROBO_DELAY = 10L
    private val FLASHLIGHT_STATE = "flashlight_state"
    private val STROBOSCOPE_STATE = "stroboscope_state"

    private var mBus: EventBus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var reTurnFlashlightOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        mBus = EventBus.getDefault()
        changeIconColor(getContrastColor(), stroboscope_btn)

        bright_display_btn.setOnClickListener {
            reTurnFlashlightOn = false
            startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
        }

        flashlight_btn.setOnClickListener {
            mCameraImpl!!.toggleFlashlight()
        }

        sos_btn.setOnClickListener {
            toggleStroboscope(true)
        }

        stroboscope_btn.setOnClickListener {
            toggleStroboscope(false)
        }

        setupStroboscope()
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        mCameraImpl!!.handleCameraSetup()
        checkState(MyCameraImpl.isFlashlightOn)

        val contrastColor = getContrastColor()
        changeIconColor(contrastColor, bright_display_btn)
        bright_display_btn.beVisibleIf(config.brightDisplay)
        sos_btn.beVisibleIf(config.sos)
        sos_btn.setTextColor(contrastColor)
        stroboscope_btn.beVisibleIf(config.stroboscope)

        if (!config.stroboscope) {
            mCameraImpl!!.stopStroboscope()
            stroboscope_bar.beInvisible()
        }

        updateTextColors(main_holder)
        if (stroboscope_bar.isInvisible()) {
            changeIconColor(contrastColor, stroboscope_btn)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        updateMenuItemColors(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FLASHLIGHT_STATE, mIsFlashlightOn)
        outState.putBoolean(STROBOSCOPE_STATE, stroboscope_bar.isVisible())
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
        reTurnFlashlightOn = false
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        reTurnFlashlightOn = false
        val licenses = LICENSE_EVENT_BUS

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
            FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
            FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
            FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons)
        )

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this)
        if (config.turnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }
    }

    private fun setupStroboscope() {
        stroboscope_bar.max = (MAX_STROBO_DELAY - MIN_STROBO_DELAY).toInt()
        stroboscope_bar.progress = config.stroboscopeProgress
        stroboscope_bar.onSeekBarChangeListener { progress ->
            val frequency = stroboscope_bar.max - progress + MIN_STROBO_DELAY
            mCameraImpl?.stroboFrequency = frequency
            config.stroboscopeFrequency = frequency
            config.stroboscopeProgress = progress
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

    private fun cameraPermissionGranted(isSOS: Boolean) {
        if (isSOS) {
            val isSOSRunning = mCameraImpl!!.toggleSOS()
            sos_btn.setTextColor(if (isSOSRunning) getAdjustedPrimaryColor() else getContrastColor())
        } else {
            if (mCameraImpl!!.toggleStroboscope()) {
                stroboscope_bar.beInvisibleIf(stroboscope_bar.isVisible())
                changeIconColor(if (stroboscope_bar.isVisible()) getAdjustedPrimaryColor() else getContrastColor(), stroboscope_btn)
            }
        }
    }

    private fun getContrastColor() = config.backgroundColor.getContrastColor()

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
        stroboscope_bar.beInvisible()
        changeIconColor(getContrastColor(), stroboscope_btn)
    }

    @Subscribe
    fun stopSOS(event: Events.StopSOS) {
        sos_btn.setTextColor(getContrastColor())
    }

    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    private fun enableFlashlight() {
        changeIconColor(getAdjustedPrimaryColor(), flashlight_btn)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = true

        sos_btn.setTextColor(getContrastColor())

        changeIconColor(getContrastColor(), stroboscope_btn)
        stroboscope_bar.beInvisible()
    }

    private fun disableFlashlight() {
        changeIconColor(getContrastColor(), flashlight_btn)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = false
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.mutate().applyColorFilter(color)
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
