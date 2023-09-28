package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.screens.SettingsScreen
import java.util.Locale

class SettingsActivity : SimpleActivity() {
    private val preferences by lazy { config }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val displayLanguage = remember { Locale.getDefault().displayLanguage }
                val turnFlashlightOnStartupFlow by preferences.turnFlashlightOnFlow.collectAsStateWithLifecycle(preferences.turnFlashlightOn)
                val forcePortraitModeFlow by preferences.forcePortraitModeFlow.collectAsStateWithLifecycle(preferences.forcePortraitMode)
                val showBrightDisplayButtonFlow by preferences.brightDisplayFlow.collectAsStateWithLifecycle(preferences.brightDisplay)
                val showSosButtonFlow by preferences.sosFlow.collectAsStateWithLifecycle(preferences.sos)
                val showStroboscopeButtonFlow by preferences.stroboscopeFlow.collectAsStateWithLifecycle(preferences.stroboscope)

                SettingsScreen(
                    displayLanguage = displayLanguage,
                    onSetupLanguagePress = ::launchChangeAppLanguageIntent,
                    customizeColors = ::startCustomizationActivity,
                    turnFlashlightOnStartupChecked = turnFlashlightOnStartupFlow,
                    forcePortraitModeChecked = forcePortraitModeFlow,
                    showBrightDisplayButtonChecked = showBrightDisplayButtonFlow,
                    showSosButtonChecked = showSosButtonFlow,
                    showStroboscopeButtonChecked = showStroboscopeButtonFlow,
                    customizeWidgetColors = {
                        Intent(this, WidgetTorchConfigureActivity::class.java).apply {
                            putExtra(IS_CUSTOMIZING_COLORS, true)
                            startActivity(this)
                        }
                    },
                    onTurnFlashlightOnStartupPress = {
                        preferences.turnFlashlightOn = it
                    },
                    onForcePortraitModePress = {
                        preferences.forcePortraitMode = it
                    },
                    onShowBrightDisplayButtonPress = {
                        preferences.brightDisplay = it
                    },
                    onShowSosButtonPress = {
                        preferences.sos = it
                    },
                    onShowStroboscopeButtonPress = {
                        preferences.stroboscope = it
                    },
                    goBack = ::finish
                )
            }
        }
    }
}
