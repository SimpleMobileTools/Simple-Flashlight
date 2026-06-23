package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.os.Handler
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.flashlight.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyCameraImpl private constructor(private val context: Context, private var cameraTorchListener: CameraTorchListener? = null) {

    companion object {
        var isFlashlightOn = false

        private var cameraFlash: CameraFlash? = null

        val cameraError = MutableSharedFlow<Unit>()

        fun newInstance(context: Context, cameraTorchListener: CameraTorchListener? = null) = MyCameraImpl(context, cameraTorchListener)
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOnFlow = _flashlightOn.asStateFlow()

    private val cameraFlash: CameraFlash?
        get() {
            if (MyCameraImpl.cameraFlash == null) {
                handleCameraSetup()
            }
            return MyCameraImpl.cameraFlash
        }

    init {
        handleCameraSetup()
    }

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        checkFlashlight()
    }

    fun handleCameraSetup() {
        try {
            if (MyCameraImpl.cameraFlash == null) {
                MyCameraImpl.cameraFlash = CameraFlash(context, cameraTorchListener)
            }
        } catch (e: Exception) {
            scope.launch {
                cameraError.emit(Unit)
            }
        }
    }

    private fun checkFlashlight() {
        handleCameraSetup()

        if (isFlashlightOn) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    fun enableFlashlight() {
        try {
            cameraFlash!!.run {
                initialize()
                toggleFlashlight(true)
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
            disableFlashlight()
            return
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(context.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        try {
            cameraFlash!!.toggleFlashlight(false)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
        stateChanged(false)
    }

    fun onTorchEnabled(isEnabled: Boolean) {
        if (isFlashlightOn != isEnabled) {
            stateChanged(isEnabled)
        }
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
        scope.launch {
            _flashlightOn.emit(isEnabled)
        }
    }

    fun releaseCamera() {
        cameraFlash.runOrToast {
            unregisterListeners()
        }

        if (isFlashlightOn) {
            disableFlashlight()
        }

        cameraFlash.runOrToast {
            release()
        }
        MyCameraImpl.cameraFlash = null
        cameraTorchListener = null

        isFlashlightOn = false
    }

    fun getMaximumBrightnessLevel(): Int {
        return cameraFlash.runOrToastWithDefault(MIN_BRIGHTNESS_LEVEL) {
            getMaximumBrightnessLevel()
        }
    }

    fun getCurrentBrightnessLevel(): Int {
        return cameraFlash.runOrToastWithDefault(DEFAULT_BRIGHTNESS_LEVEL) {
            getCurrentBrightnessLevel()
        }
    }

    fun supportsBrightnessControl(): Boolean {
        return cameraFlash.runOrToastWithDefault(false) {
            supportsBrightnessControl()
        }
    }

    fun updateBrightnessLevel(level: Int) {
        cameraFlash.runOrToast {
            changeTorchBrightness(level)
        }
    }

    fun onCameraNotAvailable() {
        disableFlashlight()
    }

    private fun <T> CameraFlash?.runOrToastWithDefault(defaultValue: T, block: CameraFlash.() -> T): T {
        return try {
            this!!.block()
        } catch (e: Exception) {
            context.showErrorToast(e)
            defaultValue
        }
    }

    private fun CameraFlash?.runOrToast(block: CameraFlash.() -> Unit) = runOrToastWithDefault(Unit, block)
}
