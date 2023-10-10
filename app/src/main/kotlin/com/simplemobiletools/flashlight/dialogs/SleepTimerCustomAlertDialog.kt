package com.simplemobiletools.flashlight.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.dialogs.DialogSurface
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SleepTimerCustomAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    onConfirmClick: (seconds: Int) -> Unit,
    onCancelClick: (() -> Unit)? = null
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var value by remember { mutableStateOf("") }
    val items = remember {
        listOf(UnitItem(R.string.minutes_raw, 60), UnitItem(R.string.seconds_raw, 1)).toImmutableList()
    }


    AlertDialog(
        onDismissRequest = alertDialogState::hide
    ) {
        DialogSurface(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(all = 24.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.extraLarge),
                    text = stringResource(id = R.string.sleep_timer),
                    style = SimpleTheme.typography.headlineSmall,
                    color = SimpleTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = SimpleTheme.dimens.padding.extraLarge
                        )
                        .padding(
                            top = SimpleTheme.dimens.padding.extraLarge
                        )
                ) {
                    TextField(
                        modifier = Modifier.padding(
                            bottom = SimpleTheme.dimens.padding.large
                        ),
                        value = value,
                        onValueChange = {
                            value = it.filter { it.isDigit() }
                        },
                        label = {
                            Text(stringResource(id = R.string.value))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    items.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == selectedItem,
                                onClick = {
                                    selectedItem = index
                                }
                            )
                            Text(
                                text = stringResource(id = item.stringResId)
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        onCancelClick?.invoke()
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = {
                        val enteredValue = Integer.valueOf(value.ifEmpty { "0" })
                        onConfirmClick(enteredValue * items[selectedItem].multiplier)
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private data class UnitItem(@StringRes val stringResId: Int, val multiplier: Int)

@Composable
@MyDevices
private fun SleepTimerCustomAlertDialogPreview() {
    AppThemeSurface {
        SleepTimerCustomAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            onConfirmClick = {},
            onCancelClick = {},
        )
    }
}
