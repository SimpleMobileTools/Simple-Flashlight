package com.simplemobiletools.flashlight.helpers

import android.content.Context
import com.google.gson.Gson

import com.simplemobiletools.flashlight.models.Timer
import com.simplemobiletools.flashlight.models.TimerState

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimerHelper(val context: Context) {
    fun getTimer(callback: (timer: Timer) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val timer: Timer

        if (sharedPreferences.contains("timer")) {
            val json = sharedPreferences.getString("timer", null)
            timer = Json.decodeFromString(json!!)
        } else {
            timer = Timer(1, 60 * 5, TimerState.Idle, null)
            val json = Json.encodeToString(timer)
            with(sharedPreferences.edit()) {
                putString("timer", json)
                apply()
            }
        }
        callback(timer)
    }

    fun insertOrUpdateTimer(timer: Timer, callback: () -> Unit = {}) {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val json = Json.encodeToString(timer)

        with(sharedPreferences.edit()) {
            putString("timer", json)
            apply()
        }
        callback()
    }
}
