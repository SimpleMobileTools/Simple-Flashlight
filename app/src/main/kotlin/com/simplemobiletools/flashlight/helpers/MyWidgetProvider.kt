package com.simplemobiletools.flashlight.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.isMarshmallowPlus
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Subscribe

class MyWidgetProvider : AppWidgetProvider() {
    private val TOGGLE = "toggle"

    companion object {
        private var mColoredBmp: Bitmap? = null
        private var mWhiteBmp: Bitmap? = null
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val views = RemoteViews(context.packageName, R.layout.widget)

            val intent = Intent(context, MyWidgetProvider::class.java)
            intent.action = TOGGLE

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent)

            val selectedColor = context.config.widgetBgColor
            val alpha = Color.alpha(selectedColor)

            mColoredBmp = getColoredCircles(context, selectedColor, alpha)
            mWhiteBmp = getColoredCircles(context, Color.WHITE, alpha)
            views.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)

            appWidgetManager.updateAppWidget(it, views)
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetProvider::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("DEBUG", "received action ${intent.action}")
        when (intent.action) {
            //TOGGLE -> toggleFlashlight(context)
            else -> super.onReceive(context, intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun toggleFlashlight(context: Context) {
        if (context.isMarshmallowPlus()) {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = manager.cameraIdList[0]
                //manager.setTorchMode(cameraId!!, enable)
            } catch (ignored: CameraAccessException) {
            }
        }
    }

    private fun getColoredCircles(context: Context, color: Int, alpha: Int): Bitmap {
        val drawable = context.resources.getDrawable(R.drawable.circles_small)
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        drawable.mutate().alpha = alpha
        return Utils.drawableToBitmap(drawable)
    }

    private fun enableFlashlight() {
        //mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mColoredBmp)
        /*for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }*/
    }

    private fun disableFlashlight() {
        //mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)
        /*for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }*/
    }

    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        if (event.isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }
}
