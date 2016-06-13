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
    private static MyCameraImpl cameraImpl;
    private static RemoteViews remoteViews;
    private static Context cxt;
    private static int[] widgetIds;
    private static AppWidgetManager widgetManager;
    private static final String TOGGLE = "toggle";
    private static Bitmap coloredBmp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initVariables(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private void initVariables(Context context) {
        cxt = context;
        final ComponentName component = new ComponentName(cxt, MyWidgetProvider.class);
        widgetManager = AppWidgetManager.getInstance(context);
        widgetIds = widgetManager.getAppWidgetIds(component);

        final Intent intent = new Intent(cxt, MyWidgetProvider.class);
        intent.setAction(TOGGLE);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(cxt, 0, intent, 0);
        remoteViews = new RemoteViews(cxt.getPackageName(), R.layout.widget);
        remoteViews.setOnClickPendingIntent(R.id.toggle_btn, pendingIntent);
        cameraImpl = new MyCameraImpl(this, cxt);

        final Resources res = cxt.getResources();
        final int appColor = res.getColor(R.color.colorPrimary);
        coloredBmp = Utils.getColoredIcon(cxt.getResources(), appColor, R.mipmap.flashlight_small);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(TOGGLE)) {
            if (cameraImpl == null) {
                initVariables(context);
            }

            cameraImpl.toggleFlashlight();
        } else
            super.onReceive(context, intent);
    }

    @Override
    public void enableFlashlight() {
        remoteViews.setImageViewBitmap(R.id.toggle_btn, coloredBmp);
        for (int widgetId : widgetIds) {
            widgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void disableFlashlight() {
        remoteViews.setImageViewResource(R.id.toggle_btn, R.mipmap.flashlight_small);
        for (int widgetId : widgetIds) {
            widgetManager.updateAppWidget(widgetId, remoteViews);
        }
        cameraImpl.releaseCamera();
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
        if (cameraImpl == null)
            initVariables(context);

        disableFlashlight();
        cameraImpl.releaseCamera();
    }
}
