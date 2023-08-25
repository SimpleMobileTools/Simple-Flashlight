package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.databinding.ActivitySettingsBinding
import com.simplemobiletools.flashlight.extensions.config
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySettingsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            updateMaterialActivityViews(settingsCoordinator, settingsHolder, useTransparentNavigation = true, useTopSearchMenu = false)
            setupMaterialScrollListener(settingsNestedScrollview, settingsToolbar)
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.settingsToolbar, NavigationIcon.Arrow)

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupCustomizeWidgetColors()
        setupUseEnglish()
        setupLanguage()
        setupTurnFlashlightOn()
        setupBrightDisplay()
        setupStroboscope()
        setupSOS()
        setupForcePortrait()
        updateTextColors(binding.settingsHolder)

        arrayOf(binding.settingsColorCustomizationSectionLabel, binding.settingsGeneralSettingsLabel).forEach {
            it.setTextColor(getProperPrimaryColor())
        }
    }

    private fun setupPurchaseThankYou() {
        binding.settingsPurchaseThankYouHolder.apply {
            beGoneIf(isOrWasThankYouInstalled())
            setOnClickListener {
                launchPurchaseThankYouIntent()
            }
        }
    }

    private fun setupCustomizeColors() {
        binding.apply {
            settingsColorCustomizationLabel.text = getCustomizeColorsString()
            settingsColorCustomizationHolder.setOnClickListener {
                handleCustomizeColorsClick()
            }
        }
    }

    private fun setupCustomizeWidgetColors() {
        binding.settingsWidgetColorCustomizationHolder.setOnClickListener {
            Intent(this, WidgetTorchConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)
                startActivity(this)
            }
        }
    }

    private fun setupUseEnglish() {
        binding.apply {
            settingsUseEnglishHolder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
            settingsUseEnglish.isChecked = config.useEnglish
            settingsUseEnglishHolder.setOnClickListener {
                settingsUseEnglish.toggle()
                config.useEnglish = settingsUseEnglish.isChecked
                exitProcess(0)
            }
        }
    }

    private fun setupLanguage() {
        binding.apply {
            settingsLanguage.text = Locale.getDefault().displayLanguage
            settingsLanguageHolder.beVisibleIf(isTiramisuPlus())
            settingsLanguageHolder.setOnClickListener {
                launchChangeAppLanguageIntent()
            }
        }
    }

    private fun setupTurnFlashlightOn() {
        binding.apply {
            settingsTurnFlashlightOn.isChecked = config.turnFlashlightOn
            settingsTurnFlashlightOnHolder.setOnClickListener {
                settingsTurnFlashlightOn.toggle()
                config.turnFlashlightOn = settingsTurnFlashlightOn.isChecked
            }
        }
    }

    private fun setupBrightDisplay() {
        binding.apply {
            settingsBrightDisplay.isChecked = config.brightDisplay
            settingsBrightDisplayHolder.setOnClickListener {
                settingsBrightDisplay.toggle()
                config.brightDisplay = settingsBrightDisplay.isChecked
            }
        }
    }

    private fun setupStroboscope() {
        binding.apply {
            settingsStroboscope.isChecked = config.stroboscope
            settingsStroboscopeHolder.setOnClickListener {
                settingsStroboscope.toggle()
                config.stroboscope = settingsStroboscope.isChecked
            }
        }
    }

    private fun setupSOS() {
        binding.apply {
            settingsSos.isChecked = config.sos
            settingsSosHolder.setOnClickListener {
                settingsSos.toggle()
                config.sos = settingsSos.isChecked
            }
        }
    }

    private fun setupForcePortrait() {
        binding.apply {
            settingsForcePortrait.isChecked = config.forcePortraitMode
            settingsForcePortraitHolder.setOnClickListener {
                settingsForcePortrait.toggle()
                config.forcePortraitMode = settingsForcePortrait.isChecked
            }
        }
    }
}
