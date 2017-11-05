package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class MarshmallowCamera constructor(val context: Context) {

    private val manager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = manager.cameraIdList[0]
        } catch (ignored: CameraAccessException) {
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun toggleMarshmallowFlashlight(bus: Bus, enable: Boolean) {
        try {
            manager.setTorchMode(cameraId!!, enable)
        } catch (e: CameraAccessException) {
            val mainRunnable = Runnable {
                bus.post(Events.CameraUnavailable())
            }
            Handler(context.mainLooper).post(mainRunnable)
        }
    }
}
