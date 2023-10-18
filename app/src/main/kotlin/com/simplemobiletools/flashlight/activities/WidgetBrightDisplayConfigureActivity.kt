package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.system_ui_controller.rememberSystemUiController
import com.simplemobiletools.commons.compose.theme.AppTheme
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.compose.theme.isLitWell
import com.simplemobiletools.commons.dialogs.ColorPickerAlertDialog
import com.simplemobiletools.commons.extensions.isUsingSystemDarkTheme
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.activities.viewmodel.WidgetConfigureViewModel
import com.simplemobiletools.flashlight.extensions.CheckFeatureLocked
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.MyWidgetBrightDisplayProvider
import com.simplemobiletools.flashlight.screens.WidgetConfigureScreen

class WidgetBrightDisplayConfigureActivity : ComponentActivity() {
    private val viewModel by viewModels<WidgetConfigureViewModel>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        viewModel.setWidgetId(intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID)

        if (viewModel.widgetId.value == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        setContent {
            AppTheme {

                val systemUiController = rememberSystemUiController()
                val surfaceColor = SimpleTheme.colorScheme.surface

                DisposableEffect(systemUiController, !isUsingSystemDarkTheme(), surfaceColor) {
                    systemUiController.setSystemBarsColor(
                        color = surfaceColor,
                        darkIcons = surfaceColor.isLitWell()
                    )
                    onDispose { }
                }

                val widgetColor by viewModel.widgetColor.collectAsStateWithLifecycle()
                val widgetAlpha by viewModel.widgetAlpha.collectAsStateWithLifecycle()

                val colorPickerDialogState = getColorPickerDialogState(widgetColor)

                WidgetConfigureScreen(
                    widgetDrawable = R.drawable.ic_bright_display_vector,
                    widgetColor = widgetColor,
                    widgetAlpha = widgetAlpha,
                    onSliderChanged = viewModel::changeAlpha,
                    onColorPressed = colorPickerDialogState::show,
                    onSavePressed = ::saveConfig
                )


                CheckFeatureLocked(skipCheck = isCustomizingColors)
            }
        }
    }

    @Composable
    private fun getColorPickerDialogState(
        @ColorInt
        widgetColor: Int
    ) = rememberAlertDialogState().apply {
        DialogMember {
            ColorPickerAlertDialog(
                alertDialogState = this,
                color = widgetColor,
                removeDimmedBackground = true,
                onActiveColorChange = {},
                onButtonPressed = { wasPositivePressed, color ->
                    if (wasPositivePressed) {
                        viewModel.updateColor(color)
                    }
                }
            )
        }
    }

    private fun saveConfig() {
        config.widgetBgColor = viewModel.widgetColor.value
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, viewModel.widgetId.value)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetBrightDisplayProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(viewModel.widgetId.value))
            sendBroadcast(this)
        }
    }
}
