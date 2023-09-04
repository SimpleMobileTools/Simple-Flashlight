package com.simplemobiletools.flashlight.models

class Events {
    class StateChanged(val isEnabled: Boolean)

    class CameraUnavailable

    class StopStroboscope

    class StopSOS

    class SleepTimerChanged(val seconds: Int)
}
