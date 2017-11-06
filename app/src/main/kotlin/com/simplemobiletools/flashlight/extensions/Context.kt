package com.simplemobiletools.flashlight.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.simplemobiletools.flashlight.helpers.Config
import com.simplemobiletools.flashlight.helpers.IS_ENABLED
import com.simplemobiletools.flashlight.helpers.MyWidgetProvider
import com.simplemobiletools.flashlight.helpers.TOGGLE_WIDGET_UI

val Context.config: Config get() = Config.newInstance(this)

fun Context.updateWidgets(isEnabled: Boolean) {
    val widgetsCnt = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, MyWidgetProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        Intent(this, MyWidgetProvider::class.java).apply {
            action = TOGGLE_WIDGET_UI
            putExtra(IS_ENABLED, isEnabled)
            sendBroadcast(this)
        }
    }
}
