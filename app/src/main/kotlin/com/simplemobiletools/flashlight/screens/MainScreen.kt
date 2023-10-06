package com.simplemobiletools.flashlight.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.AppDimensions
import com.simplemobiletools.flashlight.views.AnimatedSleepTimer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun MainScreen(
    flashlightButton: @Composable () -> Unit,
    brightDisplayButton: @Composable () -> Unit,
    sosButton: @Composable () -> Unit,
    stroboscopeButton: @Composable () -> Unit,
    slidersSection: @Composable () -> Unit,
    sleepTimer: @Composable () -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            flashlightButton()
            brightDisplayButton()
            sosButton()
            stroboscopeButton()
            slidersSection()
        }

        Box(
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            sleepTimer()
        }
    }
}

@Composable
internal fun FlashlightButton(
    flashlightActive: Boolean,
    onFlashlightPress: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .size(AppDimensions.mainButtonSize)
            .padding(vertical = SimpleTheme.dimens.margin.large)
            .clickable(
                indication = null,
                interactionSource = rememberMutableInteractionSource(),
                onClick = onFlashlightPress
            ),
        painter = painterResource(id = R.drawable.ic_flashlight_vector),
        contentDescription = stringResource(id = R.string.flashlight_short),
        tint = if (flashlightActive) SimpleTheme.colorScheme.primary else SimpleTheme.colorScheme.onSurface
    )
}

@Composable
internal fun BrightDisplayButton(
    onBrightDisplayPress: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .size(AppDimensions.smallerButtonSize)
            .padding(vertical = SimpleTheme.dimens.margin.large)
            .clickable(
                indication = null,
                interactionSource = rememberMutableInteractionSource(),
                onClick = onBrightDisplayPress
            ),
        painter = painterResource(id = R.drawable.ic_bright_display_vector),
        contentDescription = stringResource(id = R.string.bright_display),
        tint = SimpleTheme.colorScheme.onSurface
    )
}

@Composable
internal fun SosButton(
    sosActive: Boolean,
    onSosButtonPress: () -> Unit,
) {
    Text(
        modifier = Modifier
            .padding(vertical = SimpleTheme.dimens.margin.large)
            .clickable(
                indication = null,
                interactionSource = rememberMutableInteractionSource(),
                onClick = onSosButtonPress
            ),
        text = stringResource(id = R.string.sos),
        fontSize = AppDimensions.sosTextSize,
        fontWeight = FontWeight.Bold,
        color = if (sosActive) SimpleTheme.colorScheme.primary else SimpleTheme.colorScheme.onSurface
    )
}

@Composable
internal fun StroboscopeButton(
    stroboscopeActive: Boolean,
    onStroboscopeButtonPress: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .size(AppDimensions.smallerButtonSize)
            .padding(vertical = SimpleTheme.dimens.margin.large)
            .clickable(
                indication = null,
                interactionSource = rememberMutableInteractionSource(),
                onClick = onStroboscopeButtonPress
            ),
        painter = painterResource(id = R.drawable.ic_stroboscope_vector),
        contentDescription = stringResource(id = R.string.stroboscope),
        tint = if (stroboscopeActive) SimpleTheme.colorScheme.primary else SimpleTheme.colorScheme.onSurface
    )
}

@Composable
internal fun MainScreenSlidersSection(
    showBrightnessBar: Boolean,
    brightnessBarValue: Float,
    onBrightnessBarValueChange: (Float) -> Unit,
    showStroboscopeBar: Boolean,
    stroboscopeBarValue: Float,
    onStroboscopeBarValueChange: (Float) -> Unit,
) {
    val dimens = SimpleTheme.dimens
    val sliderModifier = remember {
        Modifier
            .padding(dimens.margin.extraLarge)
            .padding(vertical = dimens.margin.medium)
            .padding(bottom = dimens.margin.extraLarge)
            .size(width = AppDimensions.seekbarWidth, height = AppDimensions.seekbarHeight)
    }

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
            flashlightButton = {
                FlashlightButton(
                    onFlashlightPress = {},
                    flashlightActive = true,
                )
            },
            brightDisplayButton = {
                BrightDisplayButton(
                    onBrightDisplayPress = {}
                )
            },
            sosButton = {
                SosButton(
                    sosActive = false,
                    onSosButtonPress = {},
                )
            },
            stroboscopeButton = {
                StroboscopeButton(
                    stroboscopeActive = false,
                    onStroboscopeButtonPress = {},
                )
            },
            slidersSection = {
                MainScreenSlidersSection(
                    showBrightnessBar = false,
                    brightnessBarValue = 0f,
                    onBrightnessBarValueChange = {},
                    showStroboscopeBar = false,
                    stroboscopeBarValue = 0f,
                    onStroboscopeBarValueChange = {},
                )
            },
            sleepTimer = {
                AnimatedSleepTimer(
                    timerText = "00:00",
                    timerVisible = true,
                    onTimerClosePress = {},
                )
            },
            showMoreApps = true,
            openSettings = {},
            openAbout = {},
            moreAppsFromUs = {},
            openSleepTimer = {}
        )
    }
}
