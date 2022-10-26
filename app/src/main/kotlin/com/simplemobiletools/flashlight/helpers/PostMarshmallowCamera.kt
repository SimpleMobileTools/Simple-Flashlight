package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus

internal class PostMarshmallowCamera constructor(val context: Context) {

    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = manager.cameraIdList[0] ?: "0"
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun toggleMarshmallowFlashlight(enable: Boolean) {
        try {
            manager.setTorchMode(cameraId!!, enable)
            if (enable) {
                val brightnessLevel = getCurrentBrightnessLevel()
                changeTorchBrightness(brightnessLevel)
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
            val mainRunnable = Runnable {
                EventBus.getDefault().post(Events.CameraUnavailable())
            }
            Handler(context.mainLooper).post(mainRunnable)
        }
    }

    fun changeTorchBrightness(level: Int) {
        if (isTiramisuPlus()) {
            manager.turnOnTorchWithStrengthLevel(cameraId!!, level)
        }
    }

    fun getMaximumBrightnessLevel(): Int {
        return if (isTiramisuPlus()) {
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: MIN_BRIGHTNESS_LEVEL
        } else {
            MIN_BRIGHTNESS_LEVEL
        }
    }

    fun supportsBrightnessControl(): Boolean {
        val maxBrightnessLevel = getMaximumBrightnessLevel()
        return maxBrightnessLevel > MIN_BRIGHTNESS_LEVEL
    }

    fun getCurrentBrightnessLevel(): Int {
        var brightnessLevel = context.config.brightnessLevel
        if (brightnessLevel == DEFAULT_BRIGHTNESS_LEVEL) {
            brightnessLevel = getMaximumBrightnessLevel()
        }
        return brightnessLevel
    }
}
