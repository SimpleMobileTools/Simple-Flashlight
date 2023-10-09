package com.simplemobiletools.flashlight.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.*
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsScaffold
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.R

@Composable
internal fun SettingsScreen(
    colorCustomizationSection: @Composable () -> Unit,
    generalSection: @Composable () -> Unit,
    goBack: () -> Unit,
) {
    SettingsScaffold(title = stringResource(id = R.string.settings), goBack = goBack) {
        SettingsGroup(title = {
            SettingsTitleTextComponent(text = stringResource(id = R.string.color_customization))
        }) {
            colorCustomizationSection()
        }
        SettingsHorizontalDivider()
        SettingsGroup(title = {
            SettingsTitleTextComponent(text = stringResource(id = R.string.general_settings))
        }) {
            generalSection()
        }
    }
}

@Composable
internal fun ColorCustomizationSettingsSection(
    customizeColors: () -> Unit,
    customizeWidgetColors: () -> Unit,
) {
    SettingsPreferenceComponent(
        label = stringResource(id = R.string.customize_colors),
        doOnPreferenceClick = customizeColors,
    )
    SettingsPreferenceComponent(
        label = stringResource(id = R.string.customize_widget_colors),
        doOnPreferenceClick = customizeWidgetColors
    )
}

@Composable
internal fun GeneralSettingsSection(
    displayLanguage: String,
    turnFlashlightOnStartupChecked: Boolean,
    forcePortraitModeChecked: Boolean,
    showBrightDisplayButtonChecked: Boolean,
    showSosButtonChecked: Boolean,
    showStroboscopeButtonChecked: Boolean,
    onSetupLanguagePress: () -> Unit,
    onTurnFlashlightOnStartupPress: (Boolean) -> Unit,
    onForcePortraitModePress: (Boolean) -> Unit,
    onShowBrightDisplayButtonPress: (Boolean) -> Unit,
    onShowSosButtonPress: (Boolean) -> Unit,
    onShowStroboscopeButtonPress: (Boolean) -> Unit,
) {
    if (isTiramisuPlus()) {
        SettingsPreferenceComponent(
            label = stringResource(id = R.string.language),
            value = displayLanguage,
            doOnPreferenceClick = onSetupLanguagePress,
            preferenceValueColor = SimpleTheme.colorScheme.onSurface,
        )
    }
    SettingsCheckBoxComponent(
        label = stringResource(id = R.string.turn_flashlight_on),
        initialValue = turnFlashlightOnStartupChecked,
        onChange = onTurnFlashlightOnStartupPress
    )
    SettingsCheckBoxComponent(
        label = stringResource(id = R.string.force_portrait_mode),
        initialValue = forcePortraitModeChecked,
        onChange = onForcePortraitModePress
    )
    SettingsCheckBoxComponent(
        label = stringResource(id = R.string.show_bright_display),
        initialValue = showBrightDisplayButtonChecked,
        onChange = onShowBrightDisplayButtonPress
    )
    SettingsCheckBoxComponent(
        label = stringResource(id = R.string.show_sos),
        initialValue = showSosButtonChecked,
        onChange = onShowSosButtonPress
    )
    SettingsCheckBoxComponent(
        label = stringResource(id = R.string.show_stroboscope),
        initialValue = showStroboscopeButtonChecked,
        onChange = onShowStroboscopeButtonPress
    )
}

@Composable
@MyDevices
private fun SettingsScreenPreview() {
    AppThemeSurface {
        SettingsScreen(
            colorCustomizationSection = {
                ColorCustomizationSettingsSection(
                    customizeColors = {},
                    customizeWidgetColors = {},
                )
            },
            generalSection = {
                GeneralSettingsSection(
                    displayLanguage = "English",
                    turnFlashlightOnStartupChecked = false,
                    forcePortraitModeChecked = true,
                    showBrightDisplayButtonChecked = true,
                    showSosButtonChecked = true,
                    showStroboscopeButtonChecked = true,
                    onSetupLanguagePress = {},
                    onTurnFlashlightOnStartupPress = {},
                    onForcePortraitModePress = {},
                    onShowBrightDisplayButtonPress = {},
                    onShowSosButtonPress = {},
                    onShowStroboscopeButtonPress = {},
                )
            },
            goBack = {},
        )
    }
}

