package com.simplemobiletools.flashlight.activities

import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.flashlight.databinding.ActivityBrightDisplayBinding
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.stopSleepTimerCountDown
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BrightDisplayActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityBrightDisplayBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        setBackgroundColor(config.brightDisplayColor)

        binding.brightDisplayChangeColor.setOnClickListener {
            ColorPickerDialog(this, config.brightDisplayColor, true, currentColorCallback = {
                setBackgroundColor(it)
            }) { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    config.brightDisplayColor = color

                    val contrastColor = color.getContrastColor()
                    binding.brightDisplayChangeColor.apply {
                        setTextColor(contrastColor)
                        background.applyColorFilter(contrastColor)
                    }
                } else {
                    setBackgroundColor(config.brightDisplayColor)
                }
            }
        }

        binding.sleepTimerStop.setOnClickListener { stopSleepTimer() }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(true)
        requestedOrientation = if (config.forcePortraitMode) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(false)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun setBackgroundColor(color: Int) {
        binding.apply {
            brightDisplay.background = ColorDrawable(color)

            val contrastColor = config.brightDisplayColor.getContrastColor()
            brightDisplayChangeColor.apply {
                setTextColor(contrastColor)
                background.applyColorFilter(contrastColor)
            }
        }
    }

    private fun toggleBrightness(increase: Boolean) {
        val layout = window.attributes
        layout.screenBrightness = (if (increase) 1 else 0).toFloat()
        window.attributes = layout
    }

    private fun stopSleepTimer() {
        binding.sleepTimerHolder.fadeOut()
        stopSleepTimerCountDown()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun sleepTimerChanged(event: Events.SleepTimerChanged) {
        binding.sleepTimerValue.text = event.seconds.getFormattedDuration()
        binding.sleepTimerHolder.beVisible()

        if (event.seconds == 0) {
            finish()
        }
    }
}
