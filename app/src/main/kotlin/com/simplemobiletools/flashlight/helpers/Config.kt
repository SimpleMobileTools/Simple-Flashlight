package com.simplemobiletools.flashlight.helpers

import android.content.Context
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
}
