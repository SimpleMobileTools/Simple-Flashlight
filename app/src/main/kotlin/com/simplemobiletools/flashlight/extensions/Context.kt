package com.simplemobiletools.flashlight.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.simplemobiletools.flashlight.helpers.Config
import com.simplemobiletools.flashlight.helpers.IS_ENABLED
import com.simplemobiletools.flashlight.helpers.MyWidgetProvider
import com.simplemobiletools.flashlight.helpers.TOGGLE_WIDGET_UI

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.updateWidgets(isEnabled: Boolean) {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        Intent(applicationContext, MyWidgetProvider::class.java).apply {
            action = TOGGLE_WIDGET_UI
            putExtra(IS_ENABLED, isEnabled)
            sendBroadcast(this)
        }
    }
}

fun Context.drawableToBitmap(drawable: Drawable): Bitmap {
    val size = (60 * resources.displayMetrics.density).toInt()
    val mutableBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mutableBitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return mutableBitmap
}
