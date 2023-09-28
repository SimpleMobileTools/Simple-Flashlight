package com.simplemobiletools.flashlight.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.SettingsCheckBoxComponent
import com.simplemobiletools.commons.compose.settings.SettingsGroup
import com.simplemobiletools.commons.compose.settings.SettingsPreferenceComponent
import com.simplemobiletools.commons.compose.settings.SettingsTitleTextComponent
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsScaffold
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.divider_grey
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.R

@Composable
internal fun SettingsScreen(
    displayLanguage: String,
    turnFlashlightOnStartupChecked: Boolean,
    forcePortraitModeChecked: Boolean,
    showBrightDisplayButtonChecked: Boolean,
    showSosButtonChecked: Boolean,
    showStroboscopeButtonChecked: Boolean,
    onSetupLanguagePress: () -> Unit,
    customizeColors: () -> Unit,
    customizeWidgetColors: () -> Unit,
    onTurnFlashlightOnStartupPress: (Boolean) -> Unit,
    onForcePortraitModePress: (Boolean) -> Unit,
    onShowBrightDisplayButtonPress: (Boolean) -> Unit,
    onShowSosButtonPress: (Boolean) -> Unit,
    onShowStroboscopeButtonPress: (Boolean) -> Unit,
    goBack: () -> Unit,
) {
    SettingsScaffold(title = stringResource(id = R.string.settings), goBack = goBack) {
        SettingsGroup(title = {
            SettingsTitleTextComponent(text = stringResource(id = R.string.color_customization))
        }) {
            SettingsPreferenceComponent(
                preferenceTitle = stringResource(id = R.string.customize_colors),
                doOnPreferenceClick = customizeColors,
            )
            SettingsPreferenceComponent(
                preferenceTitle = stringResource(id = R.string.customize_widget_colors),
                doOnPreferenceClick = customizeWidgetColors
            )
        }
        HorizontalDivider(color = divider_grey)
        SettingsGroup(title = {
            SettingsTitleTextComponent(text = stringResource(id = R.string.general_settings))
        }) {

            if (isTiramisuPlus()) {
                SettingsPreferenceComponent(
                    preferenceTitle = stringResource(id = R.string.language),
                    preferenceSummary = displayLanguage,
                    doOnPreferenceClick = onSetupLanguagePress,
                    preferenceSummaryColor = MaterialTheme.colorScheme.onSurface,
                )
            }
            SettingsCheckBoxComponent(
                title = stringResource(id = R.string.turn_flashlight_on),
                initialValue = turnFlashlightOnStartupChecked,
                onChange = onTurnFlashlightOnStartupPress
            )
            SettingsCheckBoxComponent(
                title = stringResource(id = R.string.force_portrait_mode),
                initialValue = forcePortraitModeChecked,
                onChange = onForcePortraitModePress
            )
            SettingsCheckBoxComponent(
                title = stringResource(id = R.string.show_bright_display),
                initialValue = showBrightDisplayButtonChecked,
                onChange = onShowBrightDisplayButtonPress
            )
            SettingsCheckBoxComponent(
                title = stringResource(id = R.string.show_sos),
                initialValue = showSosButtonChecked,
                onChange = onShowSosButtonPress
            )
            SettingsCheckBoxComponent(
                title = stringResource(id = R.string.show_stroboscope),
                initialValue = showStroboscopeButtonChecked,
                onChange = onShowStroboscopeButtonPress
            )
        }
    }
}

@Composable
@MyDevices
private fun SettingsScreenPreview() {
    AppThemeSurface {
        SettingsScreen(
            displayLanguage = "English",
            turnFlashlightOnStartupChecked = false,
            forcePortraitModeChecked = true,
            showBrightDisplayButtonChecked = true,
            showSosButtonChecked = true,
            showStroboscopeButtonChecked = true,
            onSetupLanguagePress = {},
            customizeColors = {},
            customizeWidgetColors = {},
            onTurnFlashlightOnStartupPress = {},
            onForcePortraitModePress = {},
            onShowBrightDisplayButtonPress = {},
            onShowSosButtonPress = {},
            onShowStroboscopeButtonPress = {},
            goBack = {},
        )
    }
}

