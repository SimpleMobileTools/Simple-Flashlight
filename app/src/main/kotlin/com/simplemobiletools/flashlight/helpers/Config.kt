package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.graphics.Color
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var brightDisplay: Boolean
        get() = prefs.getBoolean(BRIGHT_DISPLAY, true)
        set(brightDisplay) = prefs.edit().putBoolean(BRIGHT_DISPLAY, brightDisplay).apply()

    var stroboscope: Boolean
        get() = prefs.getBoolean(STROBOSCOPE, true)
        set(stroboscope) = prefs.edit().putBoolean(STROBOSCOPE, stroboscope).apply()

    var sos: Boolean
        get() = prefs.getBoolean(SOS, true)
        set(sos) = prefs.edit().putBoolean(SOS, sos).apply()

    var turnFlashlightOn: Boolean
        get() = prefs.getBoolean(TURN_FLASHLIGHT_ON, false)
        set(turnFlashlightOn) = prefs.edit().putBoolean(TURN_FLASHLIGHT_ON, turnFlashlightOn).apply()

    var stroboscopeProgress: Int
        get() = prefs.getInt(STROBOSCOPE_PROGRESS, 1000)
        set(stroboscopeProgress) = prefs.edit().putInt(STROBOSCOPE_PROGRESS, stroboscopeProgress).apply()

    var stroboscopeFrequency: Long
        get() = prefs.getLong(STROBOSCOPE_FREQUENCY, 1000L)
        set(stroboscopeFrequency) = prefs.edit().putLong(STROBOSCOPE_FREQUENCY, stroboscopeFrequency).apply()

    var brightDisplayColor: Int
        get() = prefs.getInt(BRIGHT_DISPLAY_COLOR, Color.WHITE)
        set(brightDisplayColor) = prefs.edit().putInt(BRIGHT_DISPLAY_COLOR, brightDisplayColor).apply()

    var forcePortraitMode: Boolean
        get() = prefs.getBoolean(FORCE_PORTRAIT_MODE, true)
        set(forcePortraitMode) = prefs.edit().putBoolean(FORCE_PORTRAIT_MODE, forcePortraitMode).apply()
}
