package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.isThankYouInstalled
import com.simplemobiletools.commons.extensions.launchPurchaseThankYouIntent
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupCustomizeWidgetColors()
        setupUseEnglish()
        setupAvoidWhatsNew()
        setupTurnFlashlightOn()
        setupBrightDisplay()
        setupStroboscope()
        setupForcePortrait()
        updateTextColors(settings_holder)
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you_holder.beVisibleIf(config.appRunCount > 10 && !isThankYouInstalled())
        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchaseThankYouIntent()
        }
    }

    private fun setupCustomizeWidgetColors() {
        settings_customize_widget_colors_holder.setOnClickListener {
            Intent(this, WidgetConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)
                startActivity(this)
            }
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf(config.wasUseEnglishToggled || Locale.getDefault().language != "en")
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            System.exit(0)
        }
    }

    private fun setupAvoidWhatsNew() {
        settings_avoid_whats_new.isChecked = config.avoidWhatsNew
        settings_avoid_whats_new_holder.setOnClickListener {
            settings_avoid_whats_new.toggle()
            config.avoidWhatsNew = settings_avoid_whats_new.isChecked
        }
    }

    private fun setupTurnFlashlightOn() {
        settings_turn_flashlight_on.isChecked = config.turnFlashlightOn
        settings_turn_flashlight_on_holder.setOnClickListener {
            settings_turn_flashlight_on.toggle()
            config.turnFlashlightOn = settings_turn_flashlight_on.isChecked
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

    private fun setupForcePortrait() {
        settings_force_portrait.isChecked = config.forcePortraitMode
        settings_force_portrait_holder.setOnClickListener {
            settings_force_portrait.toggle()
            config.forcePortraitMode = settings_force_portrait.isChecked
        }
    }
}
