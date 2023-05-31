package com.simplemobiletools.flashlight.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.CameraTorchListener
import com.simplemobiletools.flashlight.helpers.MIN_BRIGHTNESS_LEVEL
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.simplemobiletools.flashlight.models.Events
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_timer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.system.exitProcess


class TimerActivity : SimpleActivity() {

    private val MAX_STROBO_DELAY = 2000L
    private val MIN_STROBO_DELAY = 10L
    private val FLASHLIGHT_STATE = "flashlight_state"
    private val STROBOSCOPE_STATE = "stroboscope_state"

    private var mBus: EventBus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var reTurnFlashlightOn = true

    private var countDownTimer: CountDownTimer? = null
    private var isRunning: Boolean = false
    private var mStartTimeInMillis: Long = 0
    private var mTimeLeftInMillis: Long = 0
    private var mEndTime: Long = 0
    private var brightDisp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        updateMaterialActivityViews(timer_coordinator, timer_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(timer_nested_scrollview, timer_toolbar)

        mBus = EventBus.getDefault()
        changeIconColor(getContrastColor(), stroboscope_btn_2)

        bright_display_btn_2.setOnClickListener {
            reTurnFlashlightOn = true
            startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
            brightDisp = true
            startTimer()
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

        timer.setOnClickListener {
            changeDuration(this, 0)
            isLightEnable(true)
        }

        timer_play_pause.setOnClickListener {
            if (isRunning or brightDisp == true) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        timer_reset.setOnClickListener {
            resetTimer()
        }

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

        if (config.turnFlashlightOn && reTurnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }

        reTurnFlashlightOn = true

        if (brightDisp == true){
            timer_play_pause.isEnabled = true
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FLASHLIGHT_STATE, mIsFlashlightOn)
        outState.putBoolean(STROBOSCOPE_STATE, stroboscope_bar_2.isVisible())
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

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this, object : CameraTorchListener {
            override fun onTorchEnabled(isEnabled: Boolean) {
                if (mCameraImpl!!.supportsBrightnessControl()) {
                    brightness_bar_2.beVisibleIf(isEnabled)
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
        stroboscope_bar_2.max = (MAX_STROBO_DELAY - MIN_STROBO_DELAY).toInt()
        stroboscope_bar_2.progress = config.stroboscopeProgress
        stroboscope_bar_2.onSeekBarChangeListener { progress ->
            val frequency = stroboscope_bar_2.max - progress + MIN_STROBO_DELAY
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

    private fun setupBrightness() {
        brightness_bar_2.max = mCameraImpl?.getMaximumBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
        brightness_bar_2.progress = mCameraImpl?.getCurrentBrightnessLevel() ?: MIN_BRIGHTNESS_LEVEL
        brightness_bar_2.onSeekBarChangeListener { level ->
            val newLevel = level.coerceAtLeast(MIN_BRIGHTNESS_LEVEL)
            mCameraImpl?.updateBrightnessLevel(newLevel)
            config.brightnessLevel = newLevel
        }
    }

    private fun cameraPermissionGranted(isSOS: Boolean) {
        if (isSOS) {
            val isSOSRunning = mCameraImpl!!.toggleSOS()
            if (isSOSRunning) {
                sos_btn_2.setTextColor(getProperPrimaryColor())
                timer_play_pause.isEnabled = true
            }
            else {
                sos_btn_2.setTextColor(getContrastColor())
                timer_play_pause.isEnabled = false
            }
        } else if (mCameraImpl!!.toggleStroboscope()) {
            stroboscope_bar_2.beInvisibleIf(stroboscope_bar_2.isVisible())
            if (stroboscope_bar_2.isVisible()) {
                changeIconColor(getProperPrimaryColor(),stroboscope_btn_2)
                timer_play_pause.isEnabled = true
            } else {
                changeIconColor(getContrastColor(),stroboscope_btn_2)
                timer_play_pause.isEnabled = false
            }
        }
    }

    private fun getContrastColor() = getProperBackgroundColor().getContrastColor()

    private fun releaseCamera() {
        mCameraImpl?.releaseCamera()
        mCameraImpl = null
        isRunning = false
    }
    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        checkState(event.isEnabled)
    }

    @Subscribe
    fun stopStroboscope(event: Events.StopStroboscope) {
        stroboscope_bar_2.beInvisible()
        changeIconColor(getContrastColor(), stroboscope_btn_2)
        timer_play_pause.isEnabled = false
    }

    @Subscribe
    fun stopSOS(event: Events.StopSOS) {
        sos_btn_2.setTextColor(getContrastColor())
    }
    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
            timer_play_pause.isEnabled = true
        } else {
            disableFlashlight()
            timer_play_pause.isEnabled = false
        }
    }

    private fun enableFlashlight() {
        changeIconColor(getProperPrimaryColor(), flashlight_btn_2)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = true
        sos_btn_2.setTextColor(getContrastColor())
        changeIconColor(getContrastColor(), stroboscope_btn_2)
        stroboscope_bar_2.beInvisible()
    }

    private fun disableFlashlight() {
        changeIconColor(getContrastColor(), flashlight_btn_2)
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

    private fun changeDuration(activity: SimpleActivity, time : Int) {
        MyTimePickerDialogDialog(activity, time) { seconds ->
            val timerSeconds = if (seconds <= 0) 300 else seconds
            mStartTimeInMillis = time.toLong()
            mStartTimeInMillis = timerSeconds.toLong()
            resetTimer()
        }
    }

    private fun pauseTimer() {
        timer_play_pause.setImageDrawable(getDrawable(R.drawable.ic_play_vector))
        countDownTimer!!.cancel()
        isRunning = false
        brightDisp = false
        timer_reset.visibility = View.VISIBLE
        timer.isEnabled = true
        isLightEnable(true)
    }

    private fun startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis
        countDownTimer = object : CountDownTimer(mTimeLeftInMillis*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished/1000
                updateCountDownText()

            }
            override fun onFinish() {
                finish()
                startActivity(intent)
            }
        }.start()

        isRunning = true
        timer_play_pause.setImageDrawable(getDrawable(R.drawable.ic_pause_vector))
        timer_reset.visibility = View.INVISIBLE
        timer.isEnabled = false
        isLightEnable(false)
    }

    private fun resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis
        updateCountDownText()
        timer_reset.visibility = View.INVISIBLE
    }

    private fun updateCountDownText() {
        val hours = (mTimeLeftInMillis / 3600)
        val minutes = mTimeLeftInMillis  % 3600 / 60
        val seconds = mTimeLeftInMillis % 60
        val timeLeftFormatted: String = if (hours > 0) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d", hours, minutes, seconds
            )
        } else {
            String.format(
                Locale.getDefault(),
                "%02d:%02d", minutes, seconds
            )
        }

        timer!!.text = timeLeftFormatted
    }

    private fun isLightEnable(isEnable : Boolean) {
        flashlight_btn_2.isEnabled = isEnable
        bright_display_btn_2.isEnabled = isEnable
        sos_btn_2.isEnabled = isEnable
        stroboscope_btn_2.isEnabled = isEnable
        stroboscope_bar_2.isEnabled = isEnable
    }
}


