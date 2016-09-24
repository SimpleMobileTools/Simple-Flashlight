package com.simplemobiletools.flashlight;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    private SharedPreferences mPrefs;

    public static Config newInstance(Context context) {
        return new Config(context);
    }

    private Config(Context context) {
        mPrefs = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
    }

    public boolean getIsFirstRun() {
        return mPrefs.getBoolean(Constants.IS_FIRST_RUN, true);
    }

    public void setIsFirstRun(boolean firstRun) {
        mPrefs.edit().putBoolean(Constants.IS_FIRST_RUN, firstRun).apply();
    }

    public boolean getIsDarkTheme() {
        return mPrefs.getBoolean(Constants.IS_DARK_THEME, false);
    }

    public void setIsDarkTheme(boolean isDarkTheme) {
        mPrefs.edit().putBoolean(Constants.IS_DARK_THEME, isDarkTheme).apply();
    }

    public boolean getBrightDisplay() {
        return mPrefs.getBoolean(Constants.BRIGHT_DISPLAY, true);
    }

    public void setBrightDisplay(boolean brightDisplay) {
        mPrefs.edit().putBoolean(Constants.BRIGHT_DISPLAY, brightDisplay).apply();
    }

    public boolean getStroboscope() {
        return mPrefs.getBoolean(Constants.STROBOSCOPE, true);
    }

    public void setStroboscope(boolean stroboscope) {
        mPrefs.edit().putBoolean(Constants.STROBOSCOPE, stroboscope).apply();
    }
}
