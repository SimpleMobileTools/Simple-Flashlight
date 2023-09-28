package com.simplemobiletools.flashlight.screens

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.flashlight.R

@Composable
internal fun WidgetConfigureScreen(
    widgetDrawable: Int,
    widgetColor: Int,
    widgetAlpha: Float,
    onSliderChanged: (Float) -> Unit,
    onColorPressed: () -> Unit,
    onSavePressed: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (brightDisplay, bottomControls, saveButton) = createRefs()

        Box(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.activity_margin))
                .padding(bottom = dimensionResource(id = R.dimen.activity_margin))
                .constrainAs(brightDisplay) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(bottomControls.top)
                }
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(id = R.dimen.main_button_size)),
                painter = painterResource(id = widgetDrawable),
                contentDescription = stringResource(id = R.string.bright_display),
                tint = Color(widgetColor.adjustAlpha(widgetAlpha))
            )
        }

        Row(
            modifier = Modifier.constrainAs(bottomControls) {
                bottom.linkTo(saveButton.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.widget_colorpicker_size))
                    .padding(dimensionResource(id = R.dimen.tiny_margin))
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
                    .padding(start = dimensionResource(id = R.dimen.medium_margin))
                    .background(
                        color = colorResource(id = R.color.md_grey_white),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.activity_margin))
            )
        }

        Button(
            modifier = Modifier.constrainAs(saveButton) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            onClick = onSavePressed
        ) {
            Text(text = stringResource(id = R.string.ok))
        }
    }
}

@Composable
@MyDevices
private fun WidgetBrightDisplayConfigureScreenPreview() {
    AppThemeSurface {
        WidgetConfigureScreen(
            widgetDrawable = R.drawable.ic_bright_display_vector,
            widgetColor = MaterialTheme.colorScheme.primary.toArgb(),
            widgetAlpha = 1f,
            onSliderChanged = {},
            onColorPressed = {},
            onSavePressed = {}
        )
    }
}
