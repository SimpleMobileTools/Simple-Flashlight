package com.simplemobiletools.flashlight.helpers

interface CameraFlash {
    fun initialize()
    fun toggleFlashlight(enable: Boolean)
    fun changeTorchBrightness(level: Int) {}
    fun getMaximumBrightnessLevel(): Int = DEFAULT_BRIGHTNESS_LEVEL
    fun supportsBrightnessControl(): Boolean = false
    fun getCurrentBrightnessLevel(): Int = DEFAULT_BRIGHTNESS_LEVEL
    fun unregisterListeners(){}
    fun release(){}
}
