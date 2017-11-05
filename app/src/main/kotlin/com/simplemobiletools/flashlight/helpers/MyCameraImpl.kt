package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.util.Log

import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus

import java.io.IOException

class MyCameraImpl(private val mContext: Context) {
    companion object {
        private val TAG = MyCameraImpl::class.java.simpleName

        private var mCamera: Camera? = null
        private var mParams: Camera.Parameters? = null
        private var mBus: Bus? = null

        private var mIsFlashlightOn: Boolean = false
        private var mIsMarshmallow: Boolean = false
        private var mShouldEnableFlashlight: Boolean = false
        private var mStroboFrequency: Int = 0
    }

    private var mMarshmallowCamera: MarshmallowCamera? = null
    @Volatile private var mShouldStroboscopeStop: Boolean = false
    @Volatile private var mIsStroboscopeRunning: Boolean = false

    private val isMarshmallow: Boolean
        get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

    private val stroboscope = Runnable {
        if (mIsStroboscopeRunning) {
            return@Runnable
        }

        mShouldStroboscopeStop = false
        mIsStroboscopeRunning = true

        if (Utils.isNougat) {
            while (!mShouldStroboscopeStop) {
                try {
                    mMarshmallowCamera!!.toggleMarshmallowFlashlight(mBus!!, true)
                    Thread.sleep(mStroboFrequency.toLong())
                    mMarshmallowCamera!!.toggleMarshmallowFlashlight(mBus!!, false)
                    Thread.sleep(mStroboFrequency.toLong())
                } catch (ignored: InterruptedException) {
                    mShouldStroboscopeStop = true
                } catch (ignored: RuntimeException) {
                    mShouldStroboscopeStop = true
                }

            }
        } else {
            if (mCamera == null) {
                initCamera()
            }

            val torchOn = mCamera!!.parameters
            val torchOff = mCamera!!.parameters
            torchOn.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            torchOff.flashMode = Camera.Parameters.FLASH_MODE_OFF

            val dummy = SurfaceTexture(1)
            try {
                mCamera!!.setPreviewTexture(dummy)
            } catch (e: IOException) {
                Log.e(TAG, "setup stroboscope1 " + e.message)
            }

            mCamera!!.startPreview()

            while (!mShouldStroboscopeStop) {
                try {
                    mCamera!!.parameters = torchOn
                    Thread.sleep(mStroboFrequency.toLong())
                    mCamera!!.parameters = torchOff
                    Thread.sleep(mStroboFrequency.toLong())
                } catch (ignored: InterruptedException) {
                    mShouldStroboscopeStop = true
                } catch (ignored: RuntimeException) {
                    mShouldStroboscopeStop = true
                }

            }

            try {
                if (mCamera != null) {
                    mCamera!!.parameters = torchOff
                    if (!mShouldEnableFlashlight || mIsMarshmallow) {
                        mCamera!!.release()
                        mCamera = null
                    }
                }
            } catch (e: RuntimeException) {
                Log.e(TAG, "setup stroboscope2 " + e.message)
            }

        }

        mIsStroboscopeRunning = false
        mShouldStroboscopeStop = false

        if (mShouldEnableFlashlight) {
            enableFlashlight()
            mShouldEnableFlashlight = false
        }
    }

    init {
        mIsMarshmallow = isMarshmallow
        mStroboFrequency = 1000

        if (mBus == null) {
            mBus = BusProvider.instance
            mBus!!.register(this)
        }

        handleCameraSetup()
        checkFlashlight()
    }

    fun toggleFlashlight() {
        mIsFlashlightOn = !mIsFlashlightOn
        handleCameraSetup()
    }

    fun setStroboFrequency(frequency: Int) {
        mStroboFrequency = frequency
    }

    fun toggleStroboscope(): Boolean {
        if (!mIsStroboscopeRunning)
            disableFlashlight()

        if (!Utils.isNougat) {
            if (mCamera == null) {
                initCamera()
            }

            if (mCamera == null) {
                Utils.showToast(mContext, R.string.camera_error)
                return false
            }
        }

        if (mIsStroboscopeRunning) {
            stopStroboscope()
        } else {
            Thread(stroboscope).start()
        }
        return true
    }

    fun stopStroboscope() {
        mShouldStroboscopeStop = true
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
        if (mMarshmallowCamera == null) {
            mMarshmallowCamera = MarshmallowCamera(mContext)
        }
    }

    private fun setupCamera() {
        if (mIsMarshmallow)
            return

        if (mCamera == null) {
            initCamera()
        }
    }

    private fun initCamera() {
        try {
            mCamera = Camera.open()
            mParams = mCamera!!.parameters
            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = mParams
        } catch (e: Exception) {
            Log.e(TAG, "setup mCamera " + e.message)
            mBus!!.post(Events.CameraUnavailable())
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
        mShouldStroboscopeStop = true
        if (mIsStroboscopeRunning) {
            mShouldEnableFlashlight = true
            return
        }

        mIsFlashlightOn = true
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(true)
        } else {
            if (mCamera == null || mParams == null) {
                return
            }

            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            mCamera!!.parameters = mParams
            mCamera!!.startPreview()
        }

        val mainRunnable = Runnable { mBus!!.post(Events.StateChanged(true)) }
        Handler(mContext.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        if (mIsStroboscopeRunning) {
            return
        }

        mIsFlashlightOn = false
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(false)
        } else {
            if (mCamera == null || mParams == null) {
                return
            }

            mParams!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = mParams
        }
        mBus!!.post(Events.StateChanged(false))
    }

    private fun toggleMarshmallowFlashlight(enable: Boolean) {
        mMarshmallowCamera!!.toggleMarshmallowFlashlight(mBus!!, enable)
    }

    fun releaseCamera() {
        if (mIsFlashlightOn) {
            disableFlashlight()
        }

        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }

        if (mBus != null) {
            mBus!!.unregister(this)
        }
        mIsFlashlightOn = false
        mShouldStroboscopeStop = true
    }
}
