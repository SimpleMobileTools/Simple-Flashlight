package com.simplemobiletools.flashlight.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.views.SleepTimer

@Composable
internal fun BrightDisplayScreen(
    backgroundColor: Int,
    timerText: String,
    timerVisible: Boolean,
    onChangeColorPress: () -> Unit,
    onTimerClosePress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(backgroundColor))
    ) {
        TextButton(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .border(
                    width = 1.dp,
                    color = Color(backgroundColor.getContrastColor()),
                    shape = MaterialTheme.shapes.extraLarge
                ),
            onClick = onChangeColorPress
        ) {
            Text(
                text = stringResource(id = R.string.change_color),
                color = Color(backgroundColor.getContrastColor())
            )
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding(),
            visible = timerVisible && timerText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SleepTimer(
                timerText = timerText ?: "",
                onCloseClick = onTimerClosePress
            )
        }
    }
}

@Composable
@MyDevices
private fun BrightDisplayScreenPreview() {
    AppThemeSurface {
        BrightDisplayScreen(
            backgroundColor = MaterialTheme.colorScheme.background.toArgb(),
            timerText = "00:00",
            timerVisible = true,
            onChangeColorPress = {},
            onTimerClosePress = {}
        )
    }
}
