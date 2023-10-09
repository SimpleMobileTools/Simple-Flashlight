package com.simplemobiletools.flashlight.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.compose.theme.divider_grey

@Composable
internal fun SleepTimer(
    modifier: Modifier = Modifier,
    timerText: String,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = divider_grey,
                shape = RectangleShape
            )
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = SimpleTheme.dimens.margin.large),
            text = stringResource(id = com.simplemobiletools.commons.R.string.sleep_timer),
            color = SimpleTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = timerText,
            color = SimpleTheme.colorScheme.onSurface
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(SimpleTheme.dimens.margin.medium),
            onClick = onCloseClick
        ) {
            Icon(
                painter = painterResource(id = com.simplemobiletools.commons.R.drawable.ic_cross_vector),
                contentDescription = stringResource(id = com.simplemobiletools.commons.R.string.close),
                tint = SimpleTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun AnimatedSleepTimer(
    modifier: Modifier = Modifier,
    timerText: String,
    timerVisible: Boolean,
    onTimerClosePress: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = timerVisible && timerText.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SleepTimer(
            timerText = timerText,
            onCloseClick = onTimerClosePress
        )
    }
}

@Composable
@MyDevices
internal fun SleepTimerPreview() {
    AppThemeSurface {
        SleepTimer(
            timerText = "00:00",
            onCloseClick = {}
        )
    }
}
