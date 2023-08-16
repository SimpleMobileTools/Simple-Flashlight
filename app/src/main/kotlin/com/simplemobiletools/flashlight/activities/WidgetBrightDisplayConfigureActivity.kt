package com.simplemobiletools.flashlight.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.FeatureLockedDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.databinding.WidgetBrightDisplayConfigBinding
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.MyWidgetBrightDisplayProvider

class WidgetBrightDisplayConfigureActivity : SimpleActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) { WidgetBrightDisplayConfigBinding.inflate(layoutInflater) }

    private var mWidgetAlpha = 0f
    private var mWidgetId = 0
    private var mWidgetColor = 0
    private var mWidgetColorWithoutTransparency = 0
    private var mFeatureLockedDialog: FeatureLockedDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(binding.root)
        initVariables()

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        binding.apply {
            configSave.setOnClickListener { saveConfig() }
            configWidgetColor.setOnClickListener { pickBackgroundColor() }

            val primaryColor = getProperPrimaryColor()
            configWidgetSeekbar.setColors(getProperTextColor(), primaryColor, primaryColor)
        }

        if (!isCustomizingColors && !isOrWasThankYouInstalled()) {
            mFeatureLockedDialog = FeatureLockedDialog(this) {
                if (!isOrWasThankYouInstalled()) {
                    finish()
                }
            }
        }

        binding.configSave.apply {
            backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
            setTextColor(getProperPrimaryColor().getContrastColor())
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.setBackgroundColor(0)

        if (mFeatureLockedDialog != null && isOrWasThankYouInstalled()) {
            mFeatureLockedDialog?.dismissDialog()
        }
    }

    private fun initVariables() {
        mWidgetColor = config.widgetBgColor
        if (mWidgetColor == resources.getColor(R.color.default_widget_bg_color) && config.isUsingSystemTheme) {
            mWidgetColor = resources.getColor(R.color.you_primary_color, theme)
        }

        mWidgetAlpha = Color.alpha(mWidgetColor) / 255.toFloat()

        mWidgetColorWithoutTransparency = Color.rgb(Color.red(mWidgetColor), Color.green(mWidgetColor), Color.blue(mWidgetColor))
        binding.configWidgetSeekbar.apply {
            setOnSeekBarChangeListener(seekbarChangeListener)
            progress = (mWidgetAlpha * 100).toInt()
        }
        updateColors()
    }

    private fun saveConfig() {
        config.widgetBgColor = mWidgetColor
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mWidgetColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mWidgetColorWithoutTransparency = color
                updateColors()
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetBrightDisplayProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateColors() {
        mWidgetColor = mWidgetColorWithoutTransparency.adjustAlpha(mWidgetAlpha)
        binding.apply {
            configWidgetColor.setFillWithStroke(mWidgetColor, mWidgetColor)
            configImage.background.mutate().applyColorFilter(mWidgetColor)
        }
    }

    private val seekbarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mWidgetAlpha = progress.toFloat() / 100.toFloat()
            updateColors()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }
}
