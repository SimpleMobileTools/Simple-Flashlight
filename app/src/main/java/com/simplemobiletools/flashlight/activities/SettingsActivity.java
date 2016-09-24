package com.simplemobiletools.flashlight.activities;

import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.SwitchCompat;

import com.simplemobiletools.flashlight.Config;
import com.simplemobiletools.flashlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends SimpleActivity {
    @BindView(R.id.settings_dark_theme) SwitchCompat mDarkThemeSwitch;
    @BindView(R.id.settings_bright_display) SwitchCompat mBrightDisplaySwitch;
    @BindView(R.id.settings_stroboscope) SwitchCompat mStroboscopeSwitch;

    private static Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mConfig = Config.newInstance(getApplicationContext());
        ButterKnife.bind(this);

        setupDarkTheme();
        setupBrightDisplay();
        setupStroboscope();
    }

    private void setupDarkTheme() {
        mDarkThemeSwitch.setChecked(mConfig.getIsDarkTheme());
    }

    private void setupBrightDisplay() {
        mBrightDisplaySwitch.setChecked(mConfig.getBrightDisplay());
    }

    private void setupStroboscope() {
        mStroboscopeSwitch.setChecked(mConfig.getStroboscope());
    }

    @OnClick(R.id.settings_dark_theme_holder)
    public void handleDarkTheme() {
        mDarkThemeSwitch.setChecked(!mDarkThemeSwitch.isChecked());
        mConfig.setIsDarkTheme(mDarkThemeSwitch.isChecked());
        restartActivity();
    }

    @OnClick(R.id.settings_bright_display_holder)
    public void handleBrightDisplay() {
        mBrightDisplaySwitch.setChecked(!mBrightDisplaySwitch.isChecked());
        mConfig.setBrightDisplay(mBrightDisplaySwitch.isChecked());
    }

    @OnClick(R.id.settings_stroboscope_holder)
    public void handleStroboscope() {
        mStroboscopeSwitch.setChecked(!mStroboscopeSwitch.isChecked());
        mConfig.setStroboscope(mStroboscopeSwitch.isChecked());
    }

    private void restartActivity() {
        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(getIntent()).startActivities();
    }
}
