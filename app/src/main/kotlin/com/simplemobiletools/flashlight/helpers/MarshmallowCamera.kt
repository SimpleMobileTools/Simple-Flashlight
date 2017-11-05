package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.util.Log

import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus

internal class MarshmallowCamera @TargetApi(Build.VERSION_CODES.M)
constructor(private val mContext: Context) {

    private val manager: CameraManager
    private var cameraId: String? = null

    init {
        manager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val list = manager.cameraIdList
            cameraId = list[0]
        } catch (ignored: CameraAccessException) {
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    fun toggleMarshmallowFlashlight(bus: Bus, enable: Boolean) {
        try {
            manager.setTorchMode(cameraId!!, enable)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "toggle marshmallow flashlight " + e.message)

            val mainRunnable = Runnable { bus.post(Events.CameraUnavailable()) }
            Handler(mContext.mainLooper).post(mainRunnable)
        }

    }

    companion object {
        private val TAG = MyCameraImpl::class.java.simpleName
    }
}
