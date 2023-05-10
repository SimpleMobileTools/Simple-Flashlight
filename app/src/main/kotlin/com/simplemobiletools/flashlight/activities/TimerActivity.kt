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
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*
import com.simplemobiletools.flashlight.helpers.CameraTorchListener
import org.greenrobot.eventbus.EventBus
import kotlin.system.exitProcess
import android.widget.Toast
import android.widget.TextView
import com.simplemobiletools.flashlight.helpers.MIN_BRIGHTNESS_LEVEL
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.Subscribe

class TimerActivity : SimpleActivity() {

    private val MAX_STROBO_DELAY = 2000L
    private val MIN_STROBO_DELAY = 10L
    private val FLASHLIGHT_STATE = "flashlight_state"
    private val STROBOSCOPE_STATE = "stroboscope_state"

    private var mBus: EventBus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var reTurnFlashlightOn = true
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        updateMaterialActivityViews(timer_coordinator, timer_holder, true)
        setupMaterialScrollListener(timer_nested_scrollview, timer_toolbar)

        /*val iv_click_me = findViewById(R.id.flashlight_btn_2) as ImageView
        // set on-click listener
        iv_click_me.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            Toast.makeText(this@TimerActivity, "You clicked on flashlight_btn_2.", Toast.LENGTH_SHORT).show()
        }

        val iv_click_me2 = findViewById(R.id.bright_display_btn_2) as ImageView
        // set on-click listener
        iv_click_me2.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            Toast.makeText(this@TimerActivity, "You clicked on bright_display_btn_2.", Toast.LENGTH_SHORT).show()
        }

        val iv_click_me3 = findViewById(R.id.stroboscope_btn_2) as ImageView
        // set on-click listener
        iv_click_me3.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            Toast.makeText(this@TimerActivity, "You clicked on stroboscope_btn_2.", Toast.LENGTH_SHORT).show()
        }

        val tv_click_me = findViewById(R.id.sos_btn_2) as TextView
        // set on-click listener
        tv_click_me.setOnClickListener {
            // your code to perform when the user clicks on the TextView
            Toast.makeText(this@TimerActivity, "You clicked on TextView sos_btn_2.", Toast.LENGTH_SHORT).show()
        }*/

        mBus = EventBus.getDefault()
        changeIconColor(getContrastColor(), stroboscope_btn_2)

        bright_display_btn_2.setOnClickListener {
            reTurnFlashlightOn = true
            startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
        }

        flashlight_btn_2.setOnClickListener {
            mCameraImpl!!.toggleFlashlight()
        }

        sos_btn_2.setOnClickListener {
            toggleStroboscope(true)
        }

        stroboscope_btn_2.setOnClickListener {
            toggleStroboscope(false)
        }

        setupStroboscope()
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(timer_toolbar, NavigationIcon.Arrow)

        mCameraImpl!!.handleCameraSetup()
        checkState(MyCameraImpl.isFlashlightOn)

        val contrastColor = getContrastColor()
        changeIconColor(contrastColor, bright_display_btn_2)
        bright_display_btn_2.beVisibleIf(config.brightDisplay)

        sos_btn_2.beVisibleIf(config.sos)

        if (sos_btn_2.currentTextColor != getProperPrimaryColor()) {
            sos_btn_2.setTextColor(contrastColor)
        }

        stroboscope_btn_2.beVisibleIf(config.stroboscope)

        if (!config.stroboscope) {
            mCameraImpl!!.stopStroboscope()
            stroboscope_bar_2.beInvisible()
        }

        updateTextColors(timer_holder)
        if (stroboscope_bar_2.isInvisible()) {
            changeIconColor(contrastColor, stroboscope_btn_2)
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

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this, object : CameraTorchListener {
            override fun onTorchEnabled(isEnabled: Boolean) {
                if (mCameraImpl!!.supportsBrightnessControl()) {
                    brightness_bar.beVisibleIf(isEnabled)
                }
            }
        })
        if (config.turnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }
        setupBrightness()
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

    private fun setupStroboscope() {
        stroboscope_bar_2.max = (MAX_STROBO_DELAY - MIN_STROBO_DELAY).toInt()
        stroboscope_bar_2.progress = config.stroboscopeProgress
        stroboscope_bar_2.onSeekBarChangeListener { progress ->
            val frequency = stroboscope_bar_2.max - progress + MIN_STROBO_DELAY
            mCameraImpl?.stroboFrequency = frequency
            config.stroboscopeFrequency = frequency
            config.stroboscopeProgress = progress
        }
    }

    private fun getContrastColor() = getProperBackgroundColor().getContrastColor()

    private fun releaseCamera() {
        mCameraImpl?.releaseCamera()
        mCameraImpl = null
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.mutate().applyColorFilter(color)
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
            sos_btn_2.setTextColor(if (isSOSRunning) getProperPrimaryColor() else getContrastColor())
        } else if (mCameraImpl!!.toggleStroboscope()) {
            stroboscope_bar_2.beInvisibleIf(stroboscope_bar_2.isVisible())
            changeIconColor(if (stroboscope_bar_2.isVisible()) getProperPrimaryColor() else getContrastColor(), stroboscope_btn_2)
        }
    }

    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    private fun enableFlashlight() {
        changeIconColor(getProperPrimaryColor(), flashlight_btn_2)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = true
    }

    private fun disableFlashlight() {
        changeIconColor(getContrastColor(), flashlight_btn_2)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = false
    }

    private fun setupBrightness() {
        brightness_bar_2.max = mCameraImpl?.getMaximumBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
        brightness_bar_2.progress = mCameraImpl?.getCurrentBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
        brightness_bar_2.onSeekBarChangeListener { level ->
            val newLevel = level.coerceAtLeast(MIN_BRIGHTNESS_LEVEL)
            mCameraImpl?.updateBrightnessLevel(newLevel)
            config.brightnessLevel = newLevel
        }
    }

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
