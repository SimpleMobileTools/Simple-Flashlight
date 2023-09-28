package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.FeatureLockedDialog
import com.simplemobiletools.commons.extensions.isOrWasThankYouInstalled
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.MyWidgetBrightDisplayProvider
import com.simplemobiletools.flashlight.screens.WidgetBrightDisplayConfigureScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WidgetBrightDisplayConfigureActivity : SimpleActivity() {
    private val viewModel by viewModels<WidgetBrightDisplayConfigureViewModel>()

    private var mFeatureLockedDialog: FeatureLockedDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
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

                WidgetBrightDisplayConfigureScreen(
                    widgetColor = widgetColor,
                    widgetAlpha = widgetAlpha,
                    onSliderChanged = {
                        viewModel.changeAlpha(it)
                    },
                    onColorPressed = {
                        pickBackgroundColor()
                    },
                    onSavePressed = {
                        saveConfig()
                    }
                )
            }
        }

        if (!isCustomizingColors && !isOrWasThankYouInstalled()) {
            mFeatureLockedDialog = FeatureLockedDialog(this) {
                if (!isOrWasThankYouInstalled()) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.setBackgroundColor(0)

        if (mFeatureLockedDialog != null && isOrWasThankYouInstalled()) {
            mFeatureLockedDialog?.dismissDialog()
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

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, viewModel.widgetColor.value) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                viewModel.updateColor(color)
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetBrightDisplayProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(viewModel.widgetId.value))
            sendBroadcast(this)
        }
    }

    internal class WidgetBrightDisplayConfigureViewModel(
        application: Application
    ) : AndroidViewModel(application) {


        private val _widgetAlpha = MutableStateFlow(0f)
        val widgetAlpha = _widgetAlpha.asStateFlow()

        private val _widgetId = MutableStateFlow(0)
        val widgetId = _widgetId.asStateFlow()

        private val _widgetColor = MutableStateFlow(0)
        val widgetColor = _widgetColor.asStateFlow()

        fun changeAlpha(newAlpha: Float) {
            _widgetAlpha.value = newAlpha
        }

        fun updateColor(newColor: Int) {
            _widgetColor.value = newColor
        }

        fun setWidgetId(widgetId: Int) {
            _widgetId.value = widgetId
        }

        init {
            _widgetColor.value = application.config.widgetBgColor
            if (_widgetColor.value == application.resources.getColor(R.color.default_widget_bg_color) && application.config.isUsingSystemTheme) {
                _widgetColor.value = application.resources.getColor(R.color.you_primary_color, application.theme)
            }

            _widgetAlpha.value = Color.alpha(_widgetColor.value) / 255f
        }
    }
}
