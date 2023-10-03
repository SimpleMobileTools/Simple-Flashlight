package com.simplemobiletools.flashlight.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.simplemobiletools.commons.compose.extensions.AdjustNavigationBarColors
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.menus.ActionItem
import com.simplemobiletools.commons.compose.menus.ActionMenu
import com.simplemobiletools.commons.compose.menus.OverflowMode
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.simplemobiletools.commons.compose.settings.scaffold.topAppBarColors
import com.simplemobiletools.commons.compose.settings.scaffold.topAppBarInsets
import com.simplemobiletools.commons.compose.settings.scaffold.topAppBarPaddings
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.views.SleepTimer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun MainScreen(
    timerText: String,
    timerVisible: Boolean,
    onTimerClosePress: () -> Unit,
    flashlightActive: Boolean,
    onFlashlightPress: () -> Unit,
    showBrightDisplayButton: Boolean,
    onBrightDisplayPress: () -> Unit,
    showSosButton: Boolean,
    sosActive: Boolean,
    onSosButtonPress: () -> Unit,
    showStroboscopeButton: Boolean,
    stroboscopeActive: Boolean,
    onStroboscopeButtonPress: () -> Unit,
    showBrightnessBar: Boolean,
    brightnessBarValue: Float,
    onBrightnessBarValueChange: (Float) -> Unit,
    showStroboscopeBar: Boolean,
    stroboscopeBarValue: Float,
    onStroboscopeBarValueChange: (Float) -> Unit,
    showMoreApps: Boolean,
    openSettings: () -> Unit,
    openAbout: () -> Unit,
    openSleepTimer: () -> Unit,
    moreAppsFromUs: () -> Unit,
) {
    AdjustNavigationBarColors()
    SettingsLazyScaffold(
        customTopBar = { scrolledColor: Color, _: MutableInteractionSource, scrollBehavior: TopAppBarScrollBehavior, statusBarColor: Int, colorTransitionFraction: Float, contrastColor: Color ->
            TopAppBar(
                title = {},
                actions = {
                    val actionMenus = remember { buildActionMenu(showMoreApps, openSettings, openAbout, openSleepTimer, moreAppsFromUs) }
                    var isMenuVisible by remember { mutableStateOf(false) }
                    ActionMenu(
                        items = actionMenus,
                        numIcons = 2,
                        isMenuVisible = isMenuVisible,
                        onMenuToggle = { isMenuVisible = it },
                        iconsColor = scrolledColor
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = topAppBarColors(statusBarColor, colorTransitionFraction, contrastColor),
                modifier = Modifier.topAppBarPaddings(),
                windowInsets = topAppBarInsets()
            )
        }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.main_button_size))
                    .padding(vertical = dimensionResource(id = R.dimen.normal_margin))
                    .clickable(
                        indication = null,
                        interactionSource = rememberMutableInteractionSource(),
                        onClick = onFlashlightPress
                    ),
                painter = painterResource(id = R.drawable.ic_flashlight_vector),
                contentDescription = stringResource(id = R.string.flashlight_short),
                tint = if (flashlightActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (showBrightDisplayButton) {
                Icon(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.smaller_button_size))
                        .padding(vertical = dimensionResource(id = R.dimen.normal_margin))
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = onBrightDisplayPress
                        ),
                    painter = painterResource(id = R.drawable.ic_bright_display_vector),
                    contentDescription = stringResource(id = R.string.bright_display),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showSosButton) {
                Text(
                    modifier = Modifier
                        .padding(vertical = dimensionResource(id = R.dimen.normal_margin))
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = onSosButtonPress
                        ),
                    text = stringResource(id = R.string.sos),
                    fontSize = TextUnit(dimensionResource(id = R.dimen.sos_text_size).value, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold,
                    color = if (sosActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            if (showStroboscopeButton) {
                Icon(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.smaller_button_size))
                        .padding(vertical = dimensionResource(id = R.dimen.normal_margin))
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = onStroboscopeButtonPress
                        ),
                    painter = painterResource(id = R.drawable.ic_stroboscope_vector),
                    contentDescription = "",
                    tint = if (stroboscopeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            val sliderModifier = Modifier
                .padding(dimensionResource(id = R.dimen.activity_margin))
                .padding(vertical = dimensionResource(R.dimen.medium_margin))
                .padding(bottom = dimensionResource(id = R.dimen.activity_margin))
                .size(width = dimensionResource(id = R.dimen.seekbar_width), height = dimensionResource(id = R.dimen.seekbar_height))

            if (showBrightnessBar) {
                Slider(
                    modifier = sliderModifier,
                    value = brightnessBarValue,
                    onValueChange = onBrightnessBarValueChange
                )
            }

            if (showStroboscopeBar) {
                Slider(
                    modifier = sliderModifier,
                    value = stroboscopeBarValue,
                    onValueChange = onStroboscopeBarValueChange
                )
            }

            if (!showBrightnessBar && !showStroboscopeBar) {
                Spacer(
                    modifier = sliderModifier,
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomEnd),
            visible = timerVisible && timerText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SleepTimer(
                timerText = timerText,
                onCloseClick = onTimerClosePress
            )
        }
    }
}

private fun buildActionMenu(
    showMoreApps: Boolean,
    openSettings: () -> Unit,
    openAbout: () -> Unit,
    openSleepTimer: () -> Unit,
    moreAppsFromUs: () -> Unit,
): ImmutableList<ActionItem> {
    val settings =
        ActionItem(R.string.settings, icon = Icons.Filled.Settings, doAction = openSettings, overflowMode = OverflowMode.NEVER_OVERFLOW)
    val about = ActionItem(R.string.about, icon = Icons.Outlined.Info, doAction = openAbout, overflowMode = OverflowMode.NEVER_OVERFLOW)
    val sleepTimer = ActionItem(R.string.sleep_timer, doAction = openSleepTimer, overflowMode = OverflowMode.ALWAYS_OVERFLOW)
    val list = mutableListOf(settings, about, sleepTimer)
    if (showMoreApps) {
        list += ActionItem(R.string.more_apps_from_us, doAction = moreAppsFromUs, overflowMode = OverflowMode.ALWAYS_OVERFLOW)
    }
    return list.toImmutableList()
}

@Composable
@MyDevices
internal fun MainScreenPreview() {
    AppThemeSurface {
        MainScreen(
            timerText = "00:00",
            timerVisible = true,
            onTimerClosePress = {},
            onFlashlightPress = {},
            flashlightActive = true,
            showBrightDisplayButton = true,
            onBrightDisplayPress = {},
            showSosButton = true,
            sosActive = false,
            onSosButtonPress = {},
            showStroboscopeButton = true,
            stroboscopeActive = false,
            onStroboscopeButtonPress = {},
            showBrightnessBar = false,
            brightnessBarValue = 0f,
            onBrightnessBarValueChange = {},
            showStroboscopeBar = false,
            stroboscopeBarValue = 0f,
            onStroboscopeBarValueChange = {},
            showMoreApps = true,
            openSettings = {},
            openAbout = {},
            moreAppsFromUs = {},
            openSleepTimer = {}
        )
    }
}
