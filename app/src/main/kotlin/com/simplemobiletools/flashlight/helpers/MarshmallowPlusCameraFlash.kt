package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus

internal class MarshmallowPlusCameraFlash(
    private val context: Context,
    private var cameraTorchListener: CameraTorchListener? = null,
) : CameraFlash {

    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            cameraTorchListener?.onTorchEnabled(enabled)
        }
    }

    init {
        try {
            cameraId = manager.cameraIdList[0] ?: "0"
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    override fun toggleFlashlight(enable: Boolean) {
        try {
            if (supportsBrightnessControl() && enable) {
                val brightnessLevel = getCurrentBrightnessLevel()
                changeTorchBrightness(brightnessLevel)
            } else {
                manager.setTorchMode(cameraId!!, enable)
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
            val mainRunnable = Runnable {
                EventBus.getDefault().post(Events.CameraUnavailable())
            }
            Handler(context.mainLooper).post(mainRunnable)
        }
    }

    override fun changeTorchBrightness(level: Int) {
        if (isTiramisuPlus()) {
            manager.turnOnTorchWithStrengthLevel(cameraId!!, level)
        }
    }

    override fun getMaximumBrightnessLevel(): Int {
        return if (isTiramisuPlus()) {
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: MIN_BRIGHTNESS_LEVEL
        } else {
            MIN_BRIGHTNESS_LEVEL
        }
    }

    override fun supportsBrightnessControl(): Boolean {
        val maxBrightnessLevel = getMaximumBrightnessLevel()
        return maxBrightnessLevel > MIN_BRIGHTNESS_LEVEL
    }

    override fun getCurrentBrightnessLevel(): Int {
        var brightnessLevel = context.config.brightnessLevel
        if (brightnessLevel == DEFAULT_BRIGHTNESS_LEVEL) {
            brightnessLevel = getMaximumBrightnessLevel()
        }
        return brightnessLevel
    }

    override fun initialize() {
        manager.registerTorchCallback(torchCallback, Handler(context.mainLooper))
    }

    override fun unregisterListeners() {
        manager.unregisterTorchCallback(torchCallback)
    }

    override fun release() {
        cameraTorchListener = null
    }
}
