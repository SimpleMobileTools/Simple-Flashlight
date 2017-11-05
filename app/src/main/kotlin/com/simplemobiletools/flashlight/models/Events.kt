package com.simplemobiletools.flashlight.models

class Events {
    class StateChanged(isEnabled: Boolean) {

        val isEnabled: Boolean
            get() = mIsEnabled

        init {
            mIsEnabled = isEnabled
        }

        companion object {
            private var mIsEnabled: Boolean = false
        }
    }

    class CameraUnavailable
}
