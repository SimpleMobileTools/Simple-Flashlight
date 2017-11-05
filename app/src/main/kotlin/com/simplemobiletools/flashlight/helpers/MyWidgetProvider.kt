package com.simplemobiletools.flashlight.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        initVariables(context)
        appWidgetManager.updateAppWidget(appWidgetIds, mRemoteViews)
    }

    private fun initVariables(context: Context) {
        mContext = context
        val component = ComponentName(context, MyWidgetProvider::class.java)
        mWidgetManager = AppWidgetManager.getInstance(context)
        mWidgetIds = mWidgetManager!!.getAppWidgetIds(component)

        val intent = Intent(context, MyWidgetProvider::class.java)
        intent.action = TOGGLE

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        mRemoteViews = RemoteViews(context.packageName, R.layout.widget)
        mRemoteViews!!.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent)
        mCameraImpl = MyCameraImpl(context)

        val prefs = initPrefs(context)
        val res = context.resources
        val defaultColor = res.getColor(R.color.color_primary)
        val selectedColor = prefs.getInt(WIDGET_COLOR, defaultColor)
        val alpha = Color.alpha(selectedColor)

        mColoredBmp = getColoredCircles(selectedColor, alpha)
        mWhiteBmp = getColoredCircles(Color.WHITE, alpha)
        mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)

        if (mBus == null) {
            mBus = BusProvider.instance
        }
        registerBus()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == TOGGLE) {
            if (mCameraImpl == null || mBus == null) {
                initVariables(context)
            }

            mCameraImpl!!.toggleFlashlight()
        } else
            super.onReceive(context, intent)
    }

    private fun getColoredCircles(color: Int, alpha: Int): Bitmap {
        val drawable = mContext!!.resources.getDrawable(R.drawable.circles_small)
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        drawable.mutate().alpha = alpha
        return Utils.drawableToBitmap(drawable)
    }

    fun enableFlashlight() {
        mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mColoredBmp)
        for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }
    }

    fun disableFlashlight() {
        mRemoteViews!!.setImageViewBitmap(R.id.toggle_btn, mWhiteBmp)
        for (widgetId in mWidgetIds!!) {
            mWidgetManager!!.updateAppWidget(widgetId, mRemoteViews)
        }
    }

    private fun initPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
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
        if (mCameraImpl == null)
            initVariables(context)

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

    companion object {
        private val TOGGLE = "toggle"
        private var mCameraImpl: MyCameraImpl? = null
        private var mRemoteViews: RemoteViews? = null
        private var mWidgetManager: AppWidgetManager? = null
        private var mColoredBmp: Bitmap? = null
        private var mWhiteBmp: Bitmap? = null
        private var mBus: Bus? = null
        private var mContext: Context? = null

        private var mWidgetIds: IntArray? = null
    }
}
