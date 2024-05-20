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

    val brightDisplayFlow = ::brightDisplay.asFlowNonNull(emitOnCollect = true)

    var stroboscope: Boolean
        get() = prefs.getBoolean(STROBOSCOPE, true)
        set(stroboscope) = prefs.edit().putBoolean(STROBOSCOPE, stroboscope).apply()

    val stroboscopeFlow = ::stroboscope.asFlowNonNull(emitOnCollect = true)

    var sos: Boolean
        get() = prefs.getBoolean(SOS, true)
        set(sos) = prefs.edit().putBoolean(SOS, sos).apply()

    val sosFlow = ::sos.asFlowNonNull(emitOnCollect = true)

    var turnFlashlightOn: Boolean
        get() = prefs.getBoolean(TURN_FLASHLIGHT_ON, false)
        set(turnFlashlightOn) = prefs.edit().putBoolean(TURN_FLASHLIGHT_ON, turnFlashlightOn).apply()

    val turnFlashlightOnFlow = ::turnFlashlightOn.asFlowNonNull()

    var showOnLockedScreen: Boolean
        get() = prefs.getBoolean(SHOW_ON_LOCKED_SCREEN, false)
        set(showOnLockedScreen) = prefs.edit().putBoolean(SHOW_ON_LOCKED_SCREEN, showOnLockedScreen).apply()

    val showOnLockedScreenFlow = ::showOnLockedScreen.asFlowNonNull()

    var stroboscopeProgress: Int
        get() = prefs.getInt(STROBOSCOPE_PROGRESS, 1000)
        set(stroboscopeProgress) = prefs.edit().putInt(STROBOSCOPE_PROGRESS, stroboscopeProgress).apply()

    var stroboscopeFrequency: Long
        get() = prefs.getLong(STROBOSCOPE_FREQUENCY, 1000L)
        set(stroboscopeFrequency) = prefs.edit().putLong(STROBOSCOPE_FREQUENCY, stroboscopeFrequency).apply()

    var brightDisplayColor: Int
        get() = prefs.getInt(BRIGHT_DISPLAY_COLOR, Color.WHITE)
        set(brightDisplayColor) = prefs.edit().putInt(BRIGHT_DISPLAY_COLOR, brightDisplayColor).apply()

    val brightDisplayColorFlow = ::brightDisplayColor.asFlowNonNull()

    var forcePortraitMode: Boolean
        get() = prefs.getBoolean(FORCE_PORTRAIT_MODE, true)
        set(forcePortraitMode) = prefs.edit().putBoolean(FORCE_PORTRAIT_MODE, forcePortraitMode).apply()

    val forcePortraitModeFlow = ::forcePortraitMode.asFlowNonNull()

    var brightnessLevel: Int
        get() = prefs.getInt(BRIGHTNESS_LEVEL, DEFAULT_BRIGHTNESS_LEVEL)
        set(brightnessLevel) = prefs.edit().putInt(BRIGHTNESS_LEVEL, brightnessLevel).apply()

    var lastSleepTimerSeconds: Int
        get() = prefs.getInt(LAST_SLEEP_TIMER_SECONDS, 30 * 60)
        set(lastSleepTimerSeconds) = prefs.edit().putInt(LAST_SLEEP_TIMER_SECONDS, lastSleepTimerSeconds).apply()

    val lastSleepTimerSecondsFlow = ::lastSleepTimerSeconds.asFlowNonNull()

    var sleepInTS: Long
        get() = prefs.getLong(SLEEP_IN_TS, 0)
        set(sleepInTS) = prefs.edit().putLong(SLEEP_IN_TS, sleepInTS).apply()
}
