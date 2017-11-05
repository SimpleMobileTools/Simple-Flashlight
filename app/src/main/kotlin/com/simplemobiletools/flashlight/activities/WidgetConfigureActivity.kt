package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RemoteViews
import android.widget.SeekBar
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.MyWidgetProvider
import com.simplemobiletools.flashlight.helpers.WIDGET_COLOR
import kotlinx.android.synthetic.main.widget_config.*

class WidgetConfigureActivity : AppCompatActivity() {
    companion object {
        private var mWidgetAlpha = 0f
        private var mWidgetId = 0
        private var mWidgetColor = 0
        private var mWidgetColorWithoutTransparency = 0
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_config)
        initVariables()

        val intent = intent
        val extras = intent.extras
        if (extras != null)
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish()

        config_save.setOnClickListener { saveConfig() }
        config_widget_color.setOnClickListener { pickBackgroundColor() }
    }

    private fun initVariables() {
        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        mWidgetColor = prefs.getInt(WIDGET_COLOR, 1)
        if (mWidgetColor == 1) {
            mWidgetColor = resources.getColor(R.color.color_primary)
            mWidgetAlpha = 1f
        } else {
            mWidgetAlpha = Color.alpha(mWidgetColor) / 255.toFloat()
        }

        mWidgetColorWithoutTransparency = Color.rgb(Color.red(mWidgetColor), Color.green(mWidgetColor), Color.blue(mWidgetColor))
        config_widget_seekbar.setOnSeekBarChangeListener(seekbarChangeListener)
        config_widget_seekbar.progress = (mWidgetAlpha * 100).toInt()
        updateColors()
    }

    fun saveConfig() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(packageName, R.layout.widget)
        appWidgetManager.updateAppWidget(mWidgetId, views)

        storeWidgetColors()
        requestWidgetUpdate()

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    fun pickBackgroundColor() {
        ColorPickerDialog(this, mWidgetColorWithoutTransparency) {
            mWidgetColorWithoutTransparency = it
            updateColors()
        }
    }

    private fun storeWidgetColors() {
        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putInt(WIDGET_COLOR, mWidgetColor).apply()
    }

    private fun requestWidgetUpdate() {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
        sendBroadcast(intent)
    }

    private fun updateColors() {
        mWidgetColor = mWidgetColorWithoutTransparency.adjustAlpha(mWidgetAlpha)
        config_widget_color.setBackgroundColor(mWidgetColor)
        config_image.background.mutate().setColorFilter(mWidgetColor, PorterDuff.Mode.SRC_IN)
    }

    private val seekbarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mWidgetAlpha = progress.toFloat() / 100.toFloat()
            updateColors()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }
}
