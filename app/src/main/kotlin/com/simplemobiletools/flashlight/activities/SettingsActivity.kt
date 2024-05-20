package com.simplemobiletools.flashlight.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.extensions.launchChangeAppLanguageIntent
import com.simplemobiletools.flashlight.extensions.startCustomizationActivity
import com.simplemobiletools.flashlight.screens.ColorCustomizationSettingsSection
import com.simplemobiletools.flashlight.screens.GeneralSettingsSection
import com.simplemobiletools.flashlight.screens.SettingsScreen
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : ComponentActivity() {
    private val preferences by lazy { config }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                SettingsScreen(
                    colorCustomizationSection = {
                        ColorCustomizationSettingsSection(
                            customizeColors = ::startCustomizationActivity,
                            customizeWidgetColors = {
                                Intent(this, WidgetTorchConfigureActivity::class.java).apply {
                                    putExtra(IS_CUSTOMIZING_COLORS, true)
                                    startActivity(this)
                                }
                            }
                        )
                    },
                    generalSection = {
                        val displayLanguage = remember { Locale.getDefault().displayLanguage }
                        val useEnglishChecked by preferences.useEnglishFlow.collectAsStateWithLifecycle(preferences.useEnglish)
                        val wasUseEnglishToggled by preferences.wasUseEnglishToggledFlow.collectAsStateWithLifecycle(preferences.wasUseEnglishToggled)
                        val showUseEnglish by remember {
                            derivedStateOf {
                                (wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus()
                            }
                        }
                        val turnFlashlightOnStartupFlow by preferences.turnFlashlightOnFlow.collectAsStateWithLifecycle(preferences.turnFlashlightOn)
                        val showOnLockedScreenFlow by preferences.showOnLockedScreenFlow.collectAsStateWithLifecycle(preferences.showOnLockedScreen)
                        val forcePortraitModeFlow by preferences.forcePortraitModeFlow.collectAsStateWithLifecycle(preferences.forcePortraitMode)
                        val showBrightDisplayButtonFlow by preferences.brightDisplayFlow.collectAsStateWithLifecycle(preferences.brightDisplay)
                        val showSosButtonFlow by preferences.sosFlow.collectAsStateWithLifecycle(preferences.sos)
                        val showStroboscopeButtonFlow by preferences.stroboscopeFlow.collectAsStateWithLifecycle(preferences.stroboscope)

                        GeneralSettingsSection(
                            showUseEnglish = showUseEnglish,
                            useEnglishChecked = useEnglishChecked,
                            showDisplayLanguage = isTiramisuPlus(),
                            displayLanguage = displayLanguage,
                            onUseEnglishPress = {
                                config.useEnglish = it
                                exitProcess(0)
                            },
                            onSetupLanguagePress = ::launchChangeAppLanguageIntent,
                            turnFlashlightOnStartupChecked = turnFlashlightOnStartupFlow,
                            showOnLockedScreenChecked = showOnLockedScreenFlow,
                            forcePortraitModeChecked = forcePortraitModeFlow,
                            showBrightDisplayButtonChecked = showBrightDisplayButtonFlow,
                            showSosButtonChecked = showSosButtonFlow,
                            showStroboscopeButtonChecked = showStroboscopeButtonFlow,
                            onTurnFlashlightOnStartupPress = {
                                preferences.turnFlashlightOn = it
                            },
                            onShowOnLockedScreenPress = {
                                preferences.showOnLockedScreen = it
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
                        )
                    },
                    goBack = ::finish
                )
            }
        }
    }
}
