package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isMarshmallowPlus
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.updateWidgets
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import java.io.IOException

class MyCameraImpl(val context: Context) {
    var stroboFrequency = 1000

    companion object {
        var isFlashlightOn = false

        private var camera: Camera? = null
        private var params: Camera.Parameters? = null
        private var bus: Bus? = null
        private var isMarshmallow = false
        private var shouldEnableFlashlight = false

        private var marshmallowCamera: MarshmallowCamera? = null
        @Volatile
        private var shouldStroboscopeStop = false
        @Volatile
        private var isStroboscopeRunning = false

        fun newInstance(context: Context) = MyCameraImpl(context)
    }

    init {
        isMarshmallow = isMarshmallowPlus()

        if (bus == null) {
            bus = BusProvider.instance
            bus!!.register(this)
        }

        handleCameraSetup()
    }

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        checkFlashlight()
    }

    fun toggleStroboscope(): Boolean {
        if (!isStroboscopeRunning) {
            disableFlashlight()
        }

        if (!isNougatPlus()) {
            if (camera == null) {
                initCamera()
            }

            if (camera == null) {
                context.toast(R.string.camera_error)
                return false
            }
        }

        if (isStroboscopeRunning) {
            stopStroboscope()
        } else {
            Thread(stroboscope).start()
        }
        return true
    }

    fun stopStroboscope() {
        shouldStroboscopeStop = true
    }

    fun handleCameraSetup() {
        if (isMarshmallow) {
            setupMarshmallowCamera()
        } else {
            setupCamera()
        }
    }

    private fun setupMarshmallowCamera() {
        if (marshmallowCamera == null) {
            marshmallowCamera = MarshmallowCamera(context)
        }
    }

    private fun setupCamera() {
        if (isMarshmallow)
            return

        if (camera == null) {
            initCamera()
        }
    }

    private fun initCamera() {
        try {
            camera = Camera.open()
            params = camera!!.parameters
            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        } catch (e: Exception) {
            bus!!.post(Events.CameraUnavailable())
        }

    }

    private fun checkFlashlight() {
        if (camera == null) {
            handleCameraSetup()
        }

        if (isFlashlightOn) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    fun enableFlashlight() {
        shouldStroboscopeStop = true
        if (isStroboscopeRunning) {
            shouldEnableFlashlight = true
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(true)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = params
            camera!!.startPreview()
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(context.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning) {
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(false)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        }
        stateChanged(false)
        releaseCamera()
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
        bus!!.post(Events.StateChanged(isEnabled))
        context.updateWidgets(isEnabled)
    }

    private fun toggleMarshmallowFlashlight(enable: Boolean) {
        marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, enable)
    }

    fun releaseCamera() {
        if (isFlashlightOn) {
            disableFlashlight()
        }

        camera?.release()
        camera = null

        bus?.unregister(this)
        isFlashlightOn = false
        shouldStroboscopeStop = true
    }

    private val stroboscope = Runnable {
        if (isStroboscopeRunning) {
            return@Runnable
        }

        shouldStroboscopeStop = false
        isStroboscopeRunning = true

        if (isNougatPlus()) {
            while (!shouldStroboscopeStop) {
                try {
                    marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, true)
                    Thread.sleep(stroboFrequency.toLong())
                    marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, false)
                    Thread.sleep(stroboFrequency.toLong())
                } catch (e: Exception) {
                    shouldStroboscopeStop = true
                }

            }
        } else {
            if (camera == null) {
                initCamera()
            }

            val torchOn = camera!!.parameters
            val torchOff = camera!!.parameters
            torchOn.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            torchOff.flashMode = Camera.Parameters.FLASH_MODE_OFF

            val dummy = SurfaceTexture(1)
            try {
                camera!!.setPreviewTexture(dummy)
            } catch (e: IOException) {
            }

            camera!!.startPreview()

            while (!shouldStroboscopeStop) {
                try {
                    camera!!.parameters = torchOn
                    Thread.sleep(stroboFrequency.toLong())
                    camera!!.parameters = torchOff
                    Thread.sleep(stroboFrequency.toLong())
                } catch (e: Exception) {
                }
            }

            try {
                if (camera != null) {
                    camera!!.parameters = torchOff
                    if (!shouldEnableFlashlight || isMarshmallow) {
                        camera!!.release()
                        camera = null
                    }
                }
            } catch (e: RuntimeException) {
            }
        }

        isStroboscopeRunning = false
        shouldStroboscopeStop = false

        if (shouldEnableFlashlight) {
            enableFlashlight()
            shouldEnableFlashlight = false
        }
    }
}
