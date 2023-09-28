package com.simplemobiletools.flashlight.activities.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class WidgetConfigureViewModel(
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
