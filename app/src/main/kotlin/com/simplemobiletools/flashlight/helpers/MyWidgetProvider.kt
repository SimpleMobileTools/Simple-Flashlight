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
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

class MyWidgetProvider : AppWidgetProvider() {
    private val TOGGLE = "toggle"

    companion object {
        private var mCameraImpl: MyCameraImpl? = null
        private var mRemoteViews: RemoteViews? = null
        private var mColoredBmp: Bitmap? = null
        private var mWhiteBmp: Bitmap? = null
        private var mBus: Bus? = null
        private var mContext: Context? = null
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        mContext = context
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val intent = Intent(context, MyWidgetProvider::class.java)
            intent.action = TOGGLE

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            mRemoteViews = RemoteViews(context.packageName, R.layout.widget)
            mRemoteViews!!.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent)
            mCameraImpl = MyCameraImpl(context)

            val selectedColor = context.config.widgetBgColor
            val alpha = Color.alpha(selectedColor)

            mColoredBmp = getColoredCircles(selectedColor, alpha)
            mWhiteBmp = getColoredCircles(Color.WHITE, alpha)
            mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)

            if (mBus == null) {
                mBus = BusProvider.instance
            }
            registerBus()
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetProvider::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TOGGLE -> toggleFlashlight(context)
            else -> super.onReceive(context, intent)
        }
    }

    private fun toggleFlashlight(context: Context) {
        if (mCameraImpl == null || mBus == null) {
            performUpdate(context)
        }

        mCameraImpl!!.toggleFlashlight()
    }

    private fun getColoredCircles(color: Int, alpha: Int): Bitmap {
        val drawable = mContext!!.resources.getDrawable(R.drawable.circles_small)
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        drawable.mutate().alpha = alpha
        return Utils.drawableToBitmap(drawable)
    }

    private fun enableFlashlight() {
        mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mColoredBmp)
        /*for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }*/
    }

    private fun disableFlashlight() {
        mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)
        /*for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }*/
    }

    @Subscribe
    fun cameraUnavailable(event: Events.CameraUnavailable) {
        if (mContext != null) {
            mContext!!.toast(R.string.camera_error)
            disableFlashlight()
        }
    }

    @Subscribe
    fun stateChangedEvent(event: Events.StateChanged) {
        if (event.isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        unregisterBus()
        releaseCamera(context)
    }

    private fun releaseCamera(context: Context) {
        if (mCameraImpl == null) {
            performUpdate(context)
        }

        mCameraImpl!!.releaseCamera()
    }

    private fun registerBus() {
        try {
            mBus!!.register(this)
        } catch (ignored: Exception) {
        }

    }

    private fun unregisterBus() {
        try {
            mBus!!.unregister(this)
        } catch (ignored: Exception) {
        }

    }
}
