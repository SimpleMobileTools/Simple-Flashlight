package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CameraFlash(
    private val context: Context,
    private var cameraTorchListener: CameraTorchListener? = null,
) {
    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String

    private val scope = CoroutineScope(Dispatchers.Default)

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            cameraTorchListener?.onTorchEnabled(enabled)
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            cameraTorchListener?.onTorchUnavailable()
        }
    }

    init {
        cameraId = try {
            manager.cameraIdList[0] ?: "0"
        } catch (e: Exception) {
            context.showErrorToast(e)
            "0"
        }
    }

    fun toggleFlashlight(enable: Boolean) {
        try {
            if (supportsBrightnessControl() && enable) {
                val brightnessLevel = getCurrentBrightnessLevel()
                changeTorchBrightness(brightnessLevel)
            } else {
                manager.setTorchMode(cameraId, enable)
            }
        } catch (e: Exception) {
            scope.launch {
                MyCameraImpl.cameraError.emit(Unit)
            }
            throw e
        }
    }

    fun changeTorchBrightness(level: Int) {
        if (isTiramisuPlus()) {
            manager.turnOnTorchWithStrengthLevel(cameraId, level)
        }
    }

    fun getMaximumBrightnessLevel(): Int {
        return if (isTiramisuPlus()) {
            val characteristics = manager.getCameraCharacteristics(cameraId)
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

    fun initialize() {
        manager.registerTorchCallback(torchCallback, Handler(context.mainLooper))
    }

    fun unregisterListeners() {
        manager.unregisterTorchCallback(torchCallback)
    }

    fun release() {
        cameraTorchListener = null
    }
}
