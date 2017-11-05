package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.SeekBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.MyWidgetProvider
import com.simplemobiletools.flashlight.helpers.WIDGET_COLOR
import yuku.ambilwarna.AmbilWarnaDialog

class WidgetConfigureActivity : AppCompatActivity() {
    @BindView(R.id.config_widget_seekbar) internal var mWidgetSeekBar: SeekBar? = null
    @BindView(R.id.config_widget_color) internal var mWidgetColorPicker: View? = null
    @BindView(R.id.config_image) internal var mImage: ImageView? = null

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_config)
        ButterKnife.bind(this)
        initVariables()

        val intent = intent
        val extras = intent.extras
        if (extras != null)
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish()
    }

    private fun initVariables() {
        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        mWidgetColor = prefs.getInt(WIDGET_COLOR, 1)
        if (mWidgetColor == 1) {
            mWidgetColor = resources.getColor(R.color.colorPrimary)
            mWidgetAlpha = 1f
        } else {
            mWidgetAlpha = Color.alpha(mWidgetColor) / 255.toFloat()
        }

        mWidgetColorWithoutTransparency = Color.rgb(Color.red(mWidgetColor), Color.green(mWidgetColor), Color.blue(mWidgetColor))
        mWidgetSeekBar!!.setOnSeekBarChangeListener(seekbarChangeListener)
        mWidgetSeekBar!!.progress = (mWidgetAlpha * 100).toInt()
        updateColors()
    }

    @OnClick(R.id.config_save)
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

    @OnClick(R.id.config_widget_color)
    fun pickBackgroundColor() {
        val dialog = AmbilWarnaDialog(this, mWidgetColorWithoutTransparency, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {}

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                mWidgetColorWithoutTransparency = color
                updateColors()
            }
        })

        dialog.show()
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
        mWidgetColor = adjustAlpha(mWidgetColorWithoutTransparency, mWidgetAlpha)
        mWidgetColorPicker!!.setBackgroundColor(mWidgetColor)
        mImage!!.background.mutate().setColorFilter(mWidgetColor, PorterDuff.Mode.SRC_IN)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    companion object {

        private var mWidgetAlpha: Float = 0.toFloat()
        private var mWidgetId: Int = 0
        private var mWidgetColor: Int = 0
        private var mWidgetColorWithoutTransparency: Int = 0
    }
}
