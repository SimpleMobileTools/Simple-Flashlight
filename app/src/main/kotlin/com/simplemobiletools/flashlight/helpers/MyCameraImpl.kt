package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.os.Handler
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.extensions.updateWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyCameraImpl private constructor(private val context: Context, private var cameraTorchListener: CameraTorchListener? = null) {
    var stroboFrequency = 1000L

    companion object {
        var isFlashlightOn = false

        private var u = 200L // The length of one dit (Time unit)
        private val SOS = arrayListOf(u, u, u, u, u, u * 3, u * 3, u, u * 3, u, u * 3, u * 3, u, u, u, u, u, u * 7)

        private var shouldEnableFlashlight = false
        private var shouldEnableStroboscope = false
        private var shouldEnableSOS = false
        private var isStroboSOS = false     // are we sending SOS, or casual stroboscope?

        private var cameraFlash: CameraFlash? = null

        @Volatile
        private var shouldStroboscopeStop = false

        @Volatile
        private var isStroboscopeRunning = false

        @Volatile
        private var isSOSRunning = false

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
        stroboFrequency = context.config.stroboscopeFrequency
    }

    private val _sosDisabled = MutableSharedFlow<Unit>()
    val sosDisabled = _sosDisabled.asSharedFlow()

    private val _stroboscopeDisabled = MutableSharedFlow<Unit>()
    val stroboscopeDisabled = _stroboscopeDisabled.asSharedFlow()

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        checkFlashlight()
    }

    fun toggleStroboscope(): Boolean {
        handleCameraSetup()

        if (isSOSRunning) {
            stopSOS()
            shouldEnableStroboscope = true
            return true
        }

        isStroboSOS = false
        if (!isStroboscopeRunning) {
            disableFlashlight()
        }

        cameraFlash.runOrToast {
            unregisterListeners()
        }

        if (!tryInitCamera()) {
            return false
        }

        return if (isStroboscopeRunning) {
            stopStroboscope()
            false
        } else {
            Thread(stroboscope).start()
            true
        }
    }

    fun stopStroboscope() {
        shouldStroboscopeStop = true
        scope.launch {
            _stroboscopeDisabled.emit(Unit)
        }
    }

    fun toggleSOS(): Boolean {
        handleCameraSetup()

        if (isStroboscopeRunning) {
            stopStroboscope()
            shouldEnableSOS = true
            return true
        }

        isStroboSOS = true
        if (isStroboscopeRunning) {
            stopStroboscope()
        }

        if (!tryInitCamera()) {
            return false
        }

        if (isFlashlightOn) {
            disableFlashlight()
        }

        cameraFlash.runOrToast {
            unregisterListeners()
        }

        return if (isSOSRunning) {
            stopSOS()
            false
        } else {
            Thread(stroboscope).start()
            true
        }
    }

    fun stopSOS() {
        shouldStroboscopeStop = true
        scope.launch {
            _sosDisabled.emit(Unit)
        }
    }

    private fun tryInitCamera(): Boolean {
        handleCameraSetup()
        if (cameraFlash == null) {
            context.toast(R.string.camera_error)
            return false
        }
        return true
    }

    fun handleCameraSetup() {
        try {
            if (MyCameraImpl.cameraFlash == null) {
                MyCameraImpl.cameraFlash = CameraFlash(context, cameraTorchListener)
            }
            scope.launch {
                _flashlightOn.emit(isFlashlightOn)
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
        shouldStroboscopeStop = true
        if (isStroboscopeRunning || isSOSRunning) {
            shouldEnableFlashlight = true
            return
        }

        try {
            cameraFlash.runOrToast {
                initialize()
                toggleFlashlight(true)
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
            disableFlashlight()
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(context.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning || isSOSRunning) {
            return
        }

        try {
            cameraFlash.runOrToast {
                toggleFlashlight(false)
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
            disableFlashlight()
        }
        stateChanged(false)
    }

    fun onTorchEnabled(isEnabled: Boolean) {
        if (isStroboscopeRunning || isSOSRunning) {
            return
        }
        if (isFlashlightOn != isEnabled) {
            stateChanged(isEnabled)
        }
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
        scope.launch {
            _flashlightOn.emit(isEnabled)
        }
        context.updateWidgets(isEnabled)
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
        shouldStroboscopeStop = true
    }

    private val stroboscope = Runnable {
        if (isStroboscopeRunning || isSOSRunning) {
            return@Runnable
        }

        shouldStroboscopeStop = false
        if (isStroboSOS) {
            isSOSRunning = true
        } else {
            isStroboscopeRunning = true
        }

        var sosIndex = 0
        handleCameraSetup()
        while (!shouldStroboscopeStop) {
            try {
                cameraFlash.runOrToast {
                    toggleFlashlight(true)
                }
                val onDuration = if (isStroboSOS) SOS[sosIndex++ % SOS.size] else stroboFrequency
                Thread.sleep(onDuration)
                cameraFlash.runOrToast {
                    toggleFlashlight(false)
                }
                val offDuration = if (isStroboSOS) SOS[sosIndex++ % SOS.size] else stroboFrequency
                Thread.sleep(offDuration)
            } catch (e: Exception) {
                shouldStroboscopeStop = true
            }
        }

        // disable flash immediately if stroboscope is stopped and normal flash mode is disabled
        if (shouldStroboscopeStop && !shouldEnableFlashlight) {
            handleCameraSetup()
            cameraFlash.runOrToast {
                toggleFlashlight(false)
                release()
            }
            MyCameraImpl.cameraFlash = null
        }

        shouldStroboscopeStop = false
        if (isStroboSOS) {
            isSOSRunning = false
            scope.launch {
                _sosDisabled.emit(Unit)
            }
        } else {
            isStroboscopeRunning = false
            scope.launch {
                _stroboscopeDisabled.emit(Unit)
            }
        }

        when {
            shouldEnableFlashlight -> {
                enableFlashlight()
                shouldEnableFlashlight = false
            }
            shouldEnableSOS -> {
                toggleSOS()
                shouldEnableSOS = false
            }
            shouldEnableStroboscope -> {
                toggleStroboscope()
                shouldEnableStroboscope = false
            }
        }
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
