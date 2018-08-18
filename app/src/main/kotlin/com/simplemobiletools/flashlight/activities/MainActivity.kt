package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_OTTO
import com.simplemobiletools.commons.helpers.PERMISSION_CAMERA
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.BusProvider
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : SimpleActivity() {
    private val EXTRA_TIME = 30000L
    private val MAX_STROBO_DELAY = 2000L
    private val MIN_STROBO_DELAY = 30L
    private val FLASHLIGHT_STATE = "flashlight_state"
    private val STROBOSCOPE_STATE = "stroboscope_state"

    private var mBus: Bus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false
    private var mTimeInMilliseconds: Long = 0
    private var mCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        mBus = BusProvider.instance
        changeIconColor(config.backgroundColor.getContrastColor(), stroboscope_btn)

        bright_display_btn.setOnClickListener {
            startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
        }

        flashlight_btn.setOnClickListener {
            remaining_time_chron.beVisible()
            remaining_time_chron.setTextColor(config.backgroundColor.getContrastColor())

            if (MyCameraImpl.isFlashlightOn) {
                mTimeInMilliseconds += EXTRA_TIME

                mCountDownTimer = startCountDownWithTime(mTimeInMilliseconds)
            } else {
                mCameraImpl!!.toggleFlashlight()
                remaining_time_chron.text = "∞"
                toast(getString(R.string.tap_again_to_set_timer))
            }
        }

        turn_off_flashlight_btn.setOnClickListener {
            stopFlashlight()
        }

        setupStroboscope()
    }

    private fun startCountDownWithTime(mTimeInMillis: Long): CountDownTimer? {
        val countDownInterval: Long = 500

        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeInMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // Updating the remaining time
                mTimeInMilliseconds -= countDownInterval
                remaining_time_chron.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                stopFlashlight()
            }
        }
        mCountDownTimer?.start()

        return mCountDownTimer
    }

    private fun stopFlashlight() {
        mCountDownTimer?.cancel()
        mTimeInMilliseconds = 0
        remaining_time_chron.beGone()

        if (MyCameraImpl.isFlashlightOn) {
            mCameraImpl!!.toggleFlashlight()
        }
    }

    override fun onResume() {
        super.onResume()
        mCameraImpl!!.handleCameraSetup()
        checkState(MyCameraImpl.isFlashlightOn)

        changeIconColor(config.backgroundColor.getContrastColor(), turn_off_flashlight_btn)
        changeIconColor(config.backgroundColor.getContrastColor(), bright_display_btn)
        bright_display_btn.beVisibleIf(config.brightDisplay)
        stroboscope_btn.beVisibleIf(config.stroboscope)
        if (!config.stroboscope) {
            mCameraImpl!!.stopStroboscope()
            stroboscope_bar.beInvisible()
        }

        updateTextColors(main_holder)
        if (stroboscope_bar.isInvisible()) {
            changeIconColor(config.backgroundColor.getContrastColor(), stroboscope_btn)
        }

        if (config.forcePortrait) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
            toggleStroboscope()
        }
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val faqItems = arrayListOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons)
        )

        startAboutActivity(R.string.app_name, LICENSE_OTTO, BuildConfig.VERSION_NAME, faqItems)
    }

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this)
        if (config.turnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }
    }

    private fun setupStroboscope() {
        stroboscope_btn.setOnClickListener {
            toggleStroboscope()
        }

        stroboscope_bar.max = (MAX_STROBO_DELAY - MIN_STROBO_DELAY).toInt()
        stroboscope_bar.progress = config.stroboscopeProgress
        stroboscope_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                val frequency = stroboscope_bar.max - progress + MIN_STROBO_DELAY
                mCameraImpl?.stroboFrequency = frequency
                config.stroboscopeFrequency = frequency
                config.stroboscopeProgress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    private fun toggleStroboscope() {
        // use the old Camera API for stroboscope, the new Camera Manager is way too slow
        if (isNougatPlus()) {
            cameraPermissionGranted()
        } else {
            handlePermission(PERMISSION_CAMERA) {
                if (it) {
                    cameraPermissionGranted()
                } else {
                    toast(R.string.camera_permission)
                }
            }
        }
    }

    private fun cameraPermissionGranted() {
        if (mCameraImpl!!.toggleStroboscope()) {
            stroboscope_bar.beInvisibleIf(stroboscope_bar.isVisible())
            changeIconColor(if (stroboscope_bar.isVisible()) getAdjustedPrimaryColor() else config.backgroundColor.getContrastColor(), stroboscope_btn)
        }
    }

    private fun releaseCamera() {
        mCameraImpl?.releaseCamera()
        mCameraImpl = null
    }

    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        checkState(event.isEnabled)
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

        changeIconColor(config.backgroundColor.getContrastColor(), stroboscope_btn)
        stroboscope_bar.beInvisible()
    }

    private fun disableFlashlight() {
        changeIconColor(config.backgroundColor.getContrastColor(), flashlight_btn)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = false
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.mutate().applyColorFilter(color)
    }

    @Subscribe
    fun cameraUnavailable(event: Events.CameraUnavailable) {
        toast(R.string.camera_error)
        disableFlashlight()
    }
}
