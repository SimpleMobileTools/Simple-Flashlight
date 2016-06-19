package com.simplemobiletools.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider implements MyCamera {
    private static final String TOGGLE = "toggle";
    private static MyCameraImpl mCameraImpl;
    private static RemoteViews mRemoteViews;
    private static AppWidgetManager mWidgetManager;
    private static Bitmap mColoredBmp;

    private static int[] mWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initVariables(context);
        appWidgetManager.updateAppWidget(appWidgetIds, mRemoteViews);
    }

    private void initVariables(Context context) {
        final ComponentName component = new ComponentName(context, MyWidgetProvider.class);
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetIds = mWidgetManager.getAppWidgetIds(component);

        final Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(TOGGLE);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        mRemoteViews.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent);
        mCameraImpl = new MyCameraImpl(this, context);

        final Resources res = context.getResources();
        final int appColor = res.getColor(R.color.colorPrimary);
        mColoredBmp = Utils.getColoredIcon(context.getResources(), appColor, R.mipmap.flashlight_small);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(TOGGLE)) {
            if (mCameraImpl == null) {
                initVariables(context);
            }

            mCameraImpl.toggleFlashlight();
        } else
            super.onReceive(context, intent);
    }

    @Override
    public void enableFlashlight() {
        mRemoteViews.setImageViewBitmap(R.id.toggle_btn, mColoredBmp);
        for (int widgetId : mWidgetIds) {
            mWidgetManager.updateAppWidget(widgetId, mRemoteViews);
        }
    }

    @Override
    public void disableFlashlight() {
        mRemoteViews.setImageViewResource(R.id.toggle_btn, R.mipmap.flashlight_small);
        for (int widgetId : mWidgetIds) {
            mWidgetManager.updateAppWidget(widgetId, mRemoteViews);
        }
        mCameraImpl.releaseCamera();
    }

    @Override
    public void cameraUnavailable() {
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        releaseCamera(context);
    }

    private void releaseCamera(Context context) {
        if (mCameraImpl == null)
            initVariables(context);

        disableFlashlight();
        mCameraImpl.releaseCamera();
    }
}
