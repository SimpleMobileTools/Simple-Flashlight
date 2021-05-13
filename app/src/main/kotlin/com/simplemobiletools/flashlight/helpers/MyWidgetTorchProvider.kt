package com.simplemobiletools.flashlight.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.extensions.drawableToBitmap

class MyWidgetTorchProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        val selectedColor = context.config.widgetBgColor
        val alpha = Color.alpha(selectedColor)
        val bmp = getColoredIcon(context, Color.WHITE, alpha)
        val intent = Intent(context, MyWidgetTorchProvider::class.java)
        intent.action = TOGGLE

        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val views = RemoteViews(context.packageName, R.layout.widget_torch)

            val pendingIntent = PendingIntent.getBroadcast(context, it, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.flashlight_btn, pendingIntent)
            views.setImageViewBitmap(R.id.flashlight_btn, bmp)
            appWidgetManager.updateAppWidget(it, views)
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetTorchProvider::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TOGGLE -> toggleActualFlashlight(context)
            TOGGLE_WIDGET_UI -> toggleFlashlight(context, intent)
            else -> super.onReceive(context, intent)
        }
    }

    private fun toggleActualFlashlight(context: Context) {
        MyCameraImpl.newInstance(context).toggleFlashlight()
    }

    private fun toggleFlashlight(context: Context, intent: Intent) {
        if (intent.extras?.containsKey(IS_ENABLED) == true) {
            val enable = intent.extras!!.getBoolean(IS_ENABLED)
            val widgetBgColor = context.config.widgetBgColor
            val alpha = Color.alpha(widgetBgColor)
            val selectedColor = if (enable) widgetBgColor else Color.WHITE
            val bmp = getColoredIcon(context, selectedColor, alpha)

            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
                val views = RemoteViews(context.packageName, R.layout.widget_torch)
                views.setImageViewBitmap(R.id.flashlight_btn, bmp)
                appWidgetManager.updateAppWidget(it, views)
            }
        }
    }

    private fun getColoredIcon(context: Context, color: Int, alpha: Int): Bitmap {
        val drawable = context.resources.getColoredDrawableWithColor(R.drawable.ic_flashlight, color, alpha)
        return context.drawableToBitmap(drawable)
    }
}
