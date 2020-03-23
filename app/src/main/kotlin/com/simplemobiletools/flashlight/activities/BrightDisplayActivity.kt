package com.simplemobiletools.flashlight.activities

import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.android.synthetic.main.activity_bright_display.*

class BrightDisplayActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bright_display)
        supportActionBar?.hide()
        setBackgroundColor(config.brightDisplayColor)

        bright_display_change_color.setOnClickListener {
            ColorPickerDialog(this, config.brightDisplayColor, true, currentColorCallback = {
                setBackgroundColor(it)
            }) { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    config.brightDisplayColor = color

                    val contrastColor = color.getContrastColor()
                    bright_display_change_color.setTextColor(contrastColor)
                    bright_display_change_color.background.applyColorFilter(contrastColor)
                } else {
                    setBackgroundColor(config.brightDisplayColor)
                }
            }
        }
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

    private fun setBackgroundColor(color: Int) {
        bright_display.background = ColorDrawable(color)

        val contrastColor = config.brightDisplayColor.getContrastColor()
        bright_display_change_color.setTextColor(contrastColor)
        bright_display_change_color.background.applyColorFilter(contrastColor)
    }

    private fun toggleBrightness(increase: Boolean) {
        val layout = window.attributes
        layout.screenBrightness = (if (increase) 1 else 0).toFloat()
        window.attributes = layout
    }
}
