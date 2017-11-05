package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import com.simplemobiletools.commons.extensions.isMarshmallowPlus
import com.simplemobiletools.commons.extensions.isNougatPlus
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus
import java.io.IOException

class MyCameraImpl(val context: Context) {
    var stroboFrequency = 1000

    companion object {
        private var camera: Camera? = null
        private var mParams: Camera.Parameters? = null
        private var bus: Bus? = null

        private var mIsFlashlightOn = false
        private var mIsMarshmallow = false
        private var mShouldEnableFlashlight = false
    }

    private var marshmallowCamera: MarshmallowCamera? = null
    @Volatile private var shouldStroboscopeStop = false
    @Volatile private var isStroboscopeRunning = false

    private val stroboscope = Runnable {
        if (isStroboscopeRunning) {
            return@Runnable
        }

        shouldStroboscopeStop = false
        isStroboscopeRunning = true

        if (context.isNougatPlus()) {
            while (!shouldStroboscopeStop) {
                try {
                    marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, true)
                    Thread.sleep(stroboFrequency.toLong())
                    marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, false)
                    Thread.sleep(stroboFrequency.toLong())
                } catch (ignored: InterruptedException) {
                    shouldStroboscopeStop = true
                } catch (ignored: RuntimeException) {
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
                } catch (ignored: InterruptedException) {
                    shouldStroboscopeStop = true
                } catch (ignored: RuntimeException) {
                    shouldStroboscopeStop = true
                }

            }

            try {
                if (camera != null) {
                    camera!!.parameters = torchOff
                    if (!mShouldEnableFlashlight || mIsMarshmallow) {
                        camera!!.release()
                        camera = null
                    }
                }
            } catch (e: RuntimeException) {
            }
        }

        isStroboscopeRunning = false
        shouldStroboscopeStop = false

        if (mShouldEnableFlashlight) {
            enableFlashlight()
            mShouldEnableFlashlight = false
        }
    }

    init {
        mIsMarshmallow = context.isMarshmallowPlus()

        if (bus == null) {
            bus = BusProvider.instance
            bus!!.register(this)
        }

        handleCameraSetup()
        checkFlashlight()
    }

    fun toggleFlashlight() {
        mIsFlashlightOn = !mIsFlashlightOn
        handleCameraSetup()
    }

    fun toggleStroboscope(): Boolean {
        if (!isStroboscopeRunning)
            disableFlashlight()

        if (!context.isNougatPlus()) {
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
        if (mIsMarshmallow) {
            setupMarshmallowCamera()
        } else {
            setupCamera()
        }
        checkFlashlight()
    }

    private fun setupMarshmallowCamera() {
        if (marshmallowCamera == null) {
            marshmallowCamera = MarshmallowCamera(context)
        }
    }

    private fun setupCamera() {
        if (mIsMarshmallow)
            return

        if (camera == null) {
            initCamera()
        }
    }

    private fun initCamera() {
        try {
            camera = Camera.open()
            mParams = camera!!.parameters
            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = mParams
        } catch (e: Exception) {
            bus!!.post(Events.CameraUnavailable())
        }

    }

    fun checkFlashlight() {
        if (mIsFlashlightOn) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    fun enableFlashlight() {
        shouldStroboscopeStop = true
        if (isStroboscopeRunning) {
            mShouldEnableFlashlight = true
            return
        }

        mIsFlashlightOn = true
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(true)
        } else {
            if (camera == null || mParams == null) {
                return
            }

            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = mParams
            camera!!.startPreview()
        }

        val mainRunnable = Runnable { bus!!.post(Events.StateChanged(true)) }
        Handler(context.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning) {
            return
        }

        mIsFlashlightOn = false
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(false)
        } else {
            if (camera == null || mParams == null) {
                return
            }

            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = mParams
        }
        bus!!.post(Events.StateChanged(false))
    }

    private fun toggleMarshmallowFlashlight(enable: Boolean) {
        marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, enable)
    }

    fun releaseCamera() {
        if (mIsFlashlightOn) {
            disableFlashlight()
        }

        camera?.release()
        camera = null

        bus?.unregister(this)
        mIsFlashlightOn = false
        shouldStroboscopeStop = true
    }
}
