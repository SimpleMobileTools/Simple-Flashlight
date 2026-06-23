package com.simplemobiletools.flashlight.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.AdjustNavigationBarColors
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.AppDimensions

@Composable
internal fun MainScreen(
    flashlightButton: @Composable () -> Unit,
    brightnessSlider: @Composable () -> Unit,
) {
    AdjustNavigationBarColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        flashlightButton()
        brightnessSlider()
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
            .padding(vertical = SimpleTheme.dimens.padding.large)
            .clickable(
                indication = null,
                interactionSource = rememberMutableInteractionSource(),
                onClick = onFlashlightPress
            ),
        painter = painterResource(id = R.drawable.ic_flashlight_vector),
        contentDescription = stringResource(id = com.simplemobiletools.commons.R.string.flashlight_short),
        tint = if (flashlightActive) SimpleTheme.colorScheme.primary else SimpleTheme.colorScheme.onSurface
    )
}

@Composable
internal fun BrightnessSlider(
    visible: Boolean,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    if (visible) {
        Slider(
            modifier = Modifier
                .padding(horizontal = SimpleTheme.dimens.padding.extraLarge)
                .padding(top = 24.dp)
                .size(width = AppDimensions.seekbarWidth, height = AppDimensions.seekbarHeight),
            value = value,
            onValueChange = onValueChange
        )
    }
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
            brightnessSlider = {
                BrightnessSlider(
                    visible = true,
                    value = 0.5f,
                    onValueChange = {},
                )
            },
        )
    }
}
