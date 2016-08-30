package com.simplemobiletools.flashlight.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import com.simplemobiletools.flashlight.Constants;
import com.simplemobiletools.flashlight.MyWidgetProvider;
import com.simplemobiletools.flashlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yuku.ambilwarna.AmbilWarnaDialog;

public class WidgetConfigureActivity extends AppCompatActivity {
    @BindView(R.id.config_widget_seekbar) SeekBar mWidgetSeekBar;
    @BindView(R.id.config_widget_color) View mWidgetColorPicker;
    @BindView(R.id.config_image) ImageView mImage;

    private static float mWidgetAlpha;
    private static int mWidgetId;
    private static int mWidgetColor;
    private static int mWidgetColorWithoutTransparency;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);
        ButterKnife.bind(this);
        initVariables();

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null)
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();
    }

    private void initVariables() {
        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        mWidgetColor = prefs.getInt(Constants.WIDGET_COLOR, 1);
        if (mWidgetColor == 1) {
            mWidgetColor = getResources().getColor(R.color.colorPrimary);
            mWidgetAlpha = 1.f;
        } else {
            mWidgetAlpha = Color.alpha(mWidgetColor) / (float) 255;
        }

        mWidgetColorWithoutTransparency = Color.rgb(Color.red(mWidgetColor), Color.green(mWidgetColor), Color.blue(mWidgetColor));
        mWidgetSeekBar.setOnSeekBarChangeListener(seekbarChangeListener);
        mWidgetSeekBar.setProgress((int) (mWidgetAlpha * 100));
        updateColors();
    }

    @OnClick(R.id.config_save)
    public void saveConfig() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
        appWidgetManager.updateAppWidget(mWidgetId, views);

        storeWidgetColors();
        requestWidgetUpdate();

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @OnClick(R.id.config_widget_color)
    public void pickBackgroundColor() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, mWidgetColorWithoutTransparency, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mWidgetColorWithoutTransparency = color;
                updateColors();
            }
        });

        dialog.show();
    }

    private void storeWidgetColors() {
        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.WIDGET_COLOR, mWidgetColor).apply();
    }

    private void requestWidgetUpdate() {
        final Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mWidgetId});
        sendBroadcast(intent);
    }

    private void updateColors() {
        mWidgetColor = adjustAlpha(mWidgetColorWithoutTransparency, mWidgetAlpha);
        mWidgetColorPicker.setBackgroundColor(mWidgetColor);
        mImage.getDrawable().mutate().setColorFilter(mWidgetColor, PorterDuff.Mode.SRC_IN);
    }

    private SeekBar.OnSeekBarChangeListener seekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mWidgetAlpha = (float) progress / (float) 100;
            updateColors();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private int adjustAlpha(int color, float factor) {
        final int alpha = Math.round(Color.alpha(color) * factor);
        final int red = Color.red(color);
        final int green = Color.green(color);
        final int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
