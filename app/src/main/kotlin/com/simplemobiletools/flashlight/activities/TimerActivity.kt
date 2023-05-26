package com.simplemobiletools.flashlight.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
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
//import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.flashlight.helpers.MIN_BRIGHTNESS_LEVEL
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.Subscribe
//import com.simplemobiletools.flashlight.models.Timer
import com.simplemobiletools.flashlight.models.TimerState
import com.simplemobiletools.flashlight.helpers.TimerHelper
//import com.simplemobiletools.flashlight.models.TimerEvent
import com.simplemobiletools.flashlight.extensions.getFormattedDuration
import com.simplemobiletools.flashlight.extensions.secondsToMillis
import 	android.content.Context
import android.widget.EditText
import android.view.inputmethod.InputMethodManager as InputMethodManager1
//import kotlinx.android.synthetic.main.dialog_edit_timer.view.*

class TimerActivity : SimpleActivity() {

    private val MAX_STROBO_DELAY = 2000L
    private val MIN_STROBO_DELAY = 10L
    private val FLASHLIGHT_STATE = "flashlight_state"
    private val STROBOSCOPE_STATE = "stroboscope_state"

    private var mBus: EventBus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var reTurnFlashlightOn = true

    lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false;
    var time_in_milli_seconds = 0L

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
            var time  = time_edit_text.text.toString()
            time_in_milli_seconds = time.toLong() * 60000L
            startTimer(time_in_milli_seconds)
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

        timer_play_pause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
                flashlight_btn_2.isEnabled = true
                bright_display_btn_2.isEnabled = true
                sos_btn_2.isEnabled = true
                stroboscope_btn_2.isEnabled = true
            } else {
                var time  = time_edit_text.text.toString()
                time_in_milli_seconds = time.toLong() * 60000L
                startTimer(time_in_milli_seconds)
                flashlight_btn_2.isEnabled = false
                bright_display_btn_2.isEnabled = false
                sos_btn_2.isEnabled = false
                stroboscope_btn_2.isEnabled = false
            }
        }

        timer_reset.setOnClickListener {
            resetTimer()
        }

        /*val timerHelper = TimerHelper(this)
        timerHelper.getTimer { timer ->
            val textView = findViewById<TextView>(R.id.timer_time)
            textView.text = when (timer.state) {
                is TimerState.Finished -> 0.getFormattedDuration()
                is TimerState.Idle -> timer.seconds.getFormattedDuration()
                is TimerState.Paused -> timer.state.tick.getFormattedDuration()
                is TimerState.Running -> timer.state.tick.getFormattedDuration()
            }
            val timerReset = timer_iterf.findViewById<ImageView>(R.id.timer_reset)
            timerReset.setOnClickListener {
                resetTimer(timer)
            }
            val timerPlayPause = timer_iterf.findViewById<ImageView>(R.id.timer_play_pause)
            timerPlayPause.setOnClickListener {
                /*(activity as SimpleActivity).handleNotificationPermission { granted ->
                    if (granted) {*/
                when (val state = timer.state) {
                    is TimerState.Idle -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                    is TimerState.Paused -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, state.tick))
                    is TimerState.Running -> EventBus.getDefault().post(TimerEvent.Pause(timer.id!!, state.tick))
                    is TimerState.Finished -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                }
                /*} else {
                    PermissionRequiredDialog(activity, R.string.allow_notifications_reminders)
                }
            */}
            val state = timer.state
            val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
            timerReset.beInvisibleIf(!resetPossible)
            val drawableId = if (state is TimerState.Running) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
            timerPlayPause.setImageDrawable(getDrawable(drawableId))


            val timerTimeView = timer_iterf.findViewById<TextView>(R.id.timer_time)
            timerTimeView.setOnClickListener {
                changeDuration(this, timer)
            }

        }
    }
    private fun changeDuration(activity: SimpleActivity, timer: Timer) {
        MyTimePickerDialogDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            timer.seconds = timerSeconds
            //activity.view.edit_timer_initial_time.text = timerSeconds.getFormattedDuration()
            val textView = findViewById<TextView>(R.id.timer_time)
            textView.text = timerSeconds.getFormattedDuration()
            val timerHelper = TimerHelper(this)
            timerHelper.insertOrUpdateTimer(timer)

        }*/
    }


    private fun pauseTimer() {
        timer_play_pause.setImageDrawable(getDrawable(R.drawable.ic_play_vector))
        countdown_timer.cancel()
        isRunning = false
        timer_reset.visibility = View.VISIBLE
        time_edit_text.visibility = View.VISIBLE
    }


    private fun startTimer(time_in_seconds: Long) {
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                return
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()


        isRunning = true
        timer_play_pause.setImageDrawable(getDrawable(R.drawable.ic_pause_vector))
        timer_reset.visibility = View.INVISIBLE
        time_edit_text.visibility = View.INVISIBLE

    }

    private fun resetTimer() {
        val time  = time_edit_text.text.toString()
        time_in_milli_seconds = time.toLong() * 60000L
        updateTextUI()
        timer_reset.visibility = View.INVISIBLE
    }

    private fun updateTextUI() {
        val hours = (time_in_milli_seconds / 1000) / 3600
        val minute = ((time_in_milli_seconds / 1000) %3600) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60
        if(hours > 0) {
            timer.text = "$hours:$minute:$seconds"
        }
        else {
            timer.text = "$minute:$seconds"
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
                timer_play_pause.visibility = View.VISIBLE
            }
            else {
                sos_btn_2.setTextColor(getContrastColor())
                timer_play_pause.visibility = View.INVISIBLE
            }
        } else if (mCameraImpl!!.toggleStroboscope()) {
            stroboscope_bar_2.beInvisibleIf(stroboscope_bar_2.isVisible())
            if (stroboscope_bar_2.isVisible()) {
                changeIconColor(getProperPrimaryColor(),stroboscope_btn_2)
                timer_play_pause.visibility = View.VISIBLE
            } else {
                changeIconColor(getContrastColor(),stroboscope_btn_2)
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
        stroboscope_bar_2.beInvisible()
        changeIconColor(getContrastColor(), stroboscope_btn_2)
        timer_play_pause.visibility = View.INVISIBLE
    }

    @Subscribe
    fun stopSOS(event: Events.StopSOS) {
        sos_btn_2.setTextColor(getContrastColor())
    }
    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
            timer_play_pause.visibility = View.VISIBLE
        } else {
            disableFlashlight()
            timer_play_pause.visibility = View.INVISIBLE
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

    /*@Subscribe
    fun cameraUnavailable(event: Events.CameraUnavailable) {
        toast(R.string.camera_error)
        disableFlashlight()
    }

    private fun resetTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Reset(timer.id!!))
        //simpleActivity.hideTimerNotification(timer.id!!)
    }*/

}


