package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.activities.viewmodel.WidgetConfigureViewModel
import com.simplemobiletools.flashlight.extensions.CheckFeatureLocked
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.extensions.updateBrightDisplayWidget
import com.simplemobiletools.flashlight.helpers.MyWidgetTorchProvider
import com.simplemobiletools.flashlight.screens.WidgetConfigureScreen

class WidgetTorchConfigureActivity : ComponentActivity() {
    private val viewModel by viewModels<WidgetConfigureViewModel>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        viewModel.setWidgetId(intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID)

        if (viewModel.widgetId.value == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val widgetColor by viewModel.widgetColor.collectAsStateWithLifecycle()
                val widgetAlpha by viewModel.widgetAlpha.collectAsStateWithLifecycle()

                WidgetConfigureScreen(
                    widgetDrawable = R.drawable.ic_flashlight_vector,
                    widgetColor = widgetColor,
                    widgetAlpha = widgetAlpha,
                    onSliderChanged = viewModel::changeAlpha,
                    onColorPressed = ::pickBackgroundColor,
                    onSavePressed = ::saveConfig
                )

                CheckFeatureLocked(skipCheck = isCustomizingColors)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.setBackgroundColor(0)
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

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, viewModel.widgetColor.value) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                viewModel.updateColor(color)
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetTorchProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(viewModel.widgetId.value))
            sendBroadcast(this)
        }

        updateBrightDisplayWidget()
    }
}
