package com.simplemobiletools.flashlight.models

import kotlinx.serialization.Serializable

@Serializable
sealed class TimerState {
    @Serializable
    object Idle : TimerState()
    @Serializable
    data class Running(val duration: Long, val tick: Long) : TimerState()
    @Serializable
    data class Paused(val duration: Long, val tick: Long) : TimerState()
    @Serializable
    object Finished : TimerState()
}
