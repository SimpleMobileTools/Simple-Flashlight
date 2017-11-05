package com.simplemobiletools.flashlight.activities

import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.support.v7.widget.SwitchCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.helpers.Config

class SettingsActivity : SimpleActivity() {
    @BindView(R.id.settings_bright_display) internal var mBrightDisplaySwitch: SwitchCompat? = null
    @BindView(R.id.settings_stroboscope) internal var mStroboscopeSwitch: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mConfig = Config.newInstance(applicationContext)
        ButterKnife.bind(this)

        setupBrightDisplay()
        setupStroboscope()
    }

    private fun setupBrightDisplay() {
        mBrightDisplaySwitch!!.isChecked = mConfig!!.brightDisplay
    }

    private fun setupStroboscope() {
        mStroboscopeSwitch!!.isChecked = mConfig!!.stroboscope
    }

    @OnClick(R.id.settings_bright_display_holder)
    fun handleBrightDisplay() {
        mBrightDisplaySwitch!!.isChecked = !mBrightDisplaySwitch!!.isChecked
        mConfig!!.brightDisplay = mBrightDisplaySwitch!!.isChecked
    }

    @OnClick(R.id.settings_stroboscope_holder)
    fun handleStroboscope() {
        mStroboscopeSwitch!!.isChecked = !mStroboscopeSwitch!!.isChecked
        mConfig!!.stroboscope = mStroboscopeSwitch!!.isChecked
    }

    private fun restartActivity() {
        TaskStackBuilder.create(applicationContext).addNextIntentWithParentStack(intent).startActivities()
    }

    companion object {

        private var mConfig: Config? = null
    }
}
