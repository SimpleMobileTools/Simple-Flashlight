package com.simplemobiletools.flashlight.activities

import android.os.Bundle
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupBrightDisplay()
        setupStroboscope()
        updateTextColors(settings_holder)
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupBrightDisplay() {
        settings_bright_display.isChecked = config.brightDisplay
        settings_bright_display_holder.setOnClickListener {
            settings_bright_display.toggle()
            config.brightDisplay = settings_bright_display.isChecked
        }
    }

    private fun setupStroboscope() {
        settings_stroboscope.isChecked = config.stroboscope
        settings_stroboscope_holder.setOnClickListener {
            settings_stroboscope.toggle()
            config.stroboscope = settings_stroboscope.isChecked
        }
    }
}
