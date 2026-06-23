package com.simplemobiletools.flashlight.helpers

import android.content.Context
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var brightnessLevel: Int
        get() = prefs.getInt(BRIGHTNESS_LEVEL, DEFAULT_BRIGHTNESS_LEVEL)
        set(brightnessLevel) = prefs.edit().putInt(BRIGHTNESS_LEVEL, brightnessLevel).apply()
}
