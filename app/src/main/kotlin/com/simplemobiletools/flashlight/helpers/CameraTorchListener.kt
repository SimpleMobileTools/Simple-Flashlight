package com.simplemobiletools.flashlight.helpers

interface CameraTorchListener {
    fun onTorchEnabled(isEnabled:Boolean)

    fun onTorchUnavailable()
}
