package com.simplemobiletools.flashlight.models

open class TimerState {
    object Idle : TimerState()
    data class Running(val duration: Long, val tick: Long) : TimerState()
    data class Paused(val duration: Long, val tick: Long) : TimerState()
    object Finished : TimerState()
}
