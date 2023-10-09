package com.simplemobiletools.flashlight.screens

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.AppDimensions

@Composable
internal fun WidgetConfigureScreen(
    @DrawableRes
    widgetDrawable: Int,
    @ColorInt
    widgetColor: Int,
    widgetAlpha: Float,
    onSliderChanged: (Float) -> Unit,
    onColorPressed: () -> Unit,
    onSavePressed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier = Modifier
                .padding(SimpleTheme.dimens.margin.extraLarge)
                .padding(bottom = SimpleTheme.dimens.margin.extraLarge)
                .fillMaxWidth()
                .weight(1f)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(AppDimensions.mainButtonSize),
                painter = painterResource(id = widgetDrawable),
                contentDescription = stringResource(id = R.string.bright_display),
                tint = Color(widgetColor.adjustAlpha(widgetAlpha))
            )
        }

        Row {
            Icon(
                modifier = Modifier
                    .size(AppDimensions.widgetColorPickerSize)
                    .padding(SimpleTheme.dimens.margin.extraSmall)
                    .clip(CircleShape)
                    .clickable { onColorPressed() },
                painter = BrushPainter(SolidColor(Color(widgetColor))),
                contentDescription = stringResource(id = R.string.bright_display),
                tint = Color(widgetColor)
            )

            Slider(
                value = widgetAlpha,
                onValueChange = onSliderChanged,
                modifier = Modifier
                    .padding(start = SimpleTheme.dimens.margin.medium)
                    .background(
                        color = colorResource(id = com.simplemobiletools.commons.R.color.md_grey_white),
                        shape = SimpleTheme.shapes.extraLarge
                    )
                    .padding(horizontal = SimpleTheme.dimens.margin.extraLarge)
            )
        }

        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = onSavePressed
        ) {
            Text(text = stringResource(id = com.simplemobiletools.commons.R.string.ok))
        }
    }
}

@Composable
@MyDevices
private fun WidgetBrightDisplayConfigureScreenPreview() {
    AppThemeSurface {
        WidgetConfigureScreen(
            widgetDrawable = R.drawable.ic_bright_display_vector,
            widgetColor = SimpleTheme.colorScheme.primary.toArgb(),
            widgetAlpha = 1f,
            onSliderChanged = {},
            onColorPressed = {},
            onSavePressed = {}
        )
    }
}
