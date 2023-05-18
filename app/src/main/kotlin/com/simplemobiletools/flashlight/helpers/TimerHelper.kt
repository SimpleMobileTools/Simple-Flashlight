package com.simplemobiletools.flashlight.helpers

import android.content.Context
import com.google.gson.Gson

import com.simplemobiletools.flashlight.models.Timer
import com.simplemobiletools.flashlight.models.TimerState

class TimerHelper(val context: Context) {

    fun getTimer(callback: (timer: Timer) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val timer: Timer
        if (sharedPreferences.contains("timer")) {
            val json = sharedPreferences.getString("timer", null)
            val gson = Gson()
            timer = gson.fromJson(json, Timer::class.java)
        } else {
            timer = Timer(1, 60 * 5, TimerState.Paused(duration = 0, tick = 0), null)
            val gson = Gson()
            val json = gson.toJson(timer)
            with(sharedPreferences.edit()) {
                putString("timer", json)
                apply()
            }
        }
        callback(timer)
    }

    fun insertOrUpdateTimer(timer: Timer, callback: () -> Unit = {}) {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(timer)
        with(sharedPreferences.edit()) {
            putString("timer", json)
            apply()
        }
        callback()
    }
}

