package com.simplemobiletools.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String TOGGLE = "toggle";
    private static MyCameraImpl mCameraImpl;
    private static RemoteViews mRemoteViews;
    private static AppWidgetManager mWidgetManager;
    private static Bitmap mColoredBmp;
    private static Bus mBus;
    private static Context mContext;

    private static int[] mWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initVariables(context);
        appWidgetManager.updateAppWidget(appWidgetIds, mRemoteViews);
    }

    private void initVariables(Context context) {
        mContext = context;
        final ComponentName component = new ComponentName(context, MyWidgetProvider.class);
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetIds = mWidgetManager.getAppWidgetIds(component);

        final Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(TOGGLE);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        mRemoteViews.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent);
        mCameraImpl = new MyCameraImpl(context);

        final SharedPreferences prefs = initPrefs(context);
        final Resources res = context.getResources();
        final int defaultColor = res.getColor(R.color.colorPrimary);
        final int appColor = prefs.getInt(Constants.WIDGET_COLOR, defaultColor);

        final Drawable drawable = context.getResources().getDrawable(R.drawable.circles_small);
        drawable.mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_ATOP);
        mColoredBmp = Utils.drawableToBitmap(drawable);

        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        registerBus();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(TOGGLE)) {
            if (mCameraImpl == null || mBus == null) {
                initVariables(context);
            }

            mCameraImpl.toggleFlashlight();
        } else
            super.onReceive(context, intent);
    }

    public void enableFlashlight() {
        mRemoteViews.setImageViewBitmap(R.id.toggle_btn, mColoredBmp);
        for (int widgetId : mWidgetIds) {
            mWidgetManager.updateAppWidget(widgetId, mRemoteViews);
        }
    }

    public void disableFlashlight() {
        mRemoteViews.setImageViewResource(R.id.toggle_btn, R.drawable.circles_small);
        for (int widgetId : mWidgetIds) {
            mWidgetManager.updateAppWidget(widgetId, mRemoteViews);
        }
    }

    private SharedPreferences initPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
    }

    @Subscribe
    public void cameraUnavailable(Events.CameraUnavailable event) {
        if (mContext != null) {
            Utils.showToast(mContext, R.string.camera_error);
            disableFlashlight();
        }
    }

    @Subscribe
    public void stateChangedEvent(Events.StateChanged event) {
        if (event.getIsEnabled()) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        unregisterBus();
        releaseCamera(context);
    }

    private void releaseCamera(Context context) {
        if (mCameraImpl == null)
            initVariables(context);

        mCameraImpl.releaseCamera();
    }

    private void registerBus() {
        try {
            mBus.register(this);
        } catch (Exception ignored) {
        }
    }

    private void unregisterBus() {
        try {
            mBus.unregister(this);
        } catch (Exception ignored) {
        }
    }
}
