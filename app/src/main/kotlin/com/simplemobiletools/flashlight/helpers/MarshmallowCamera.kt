package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus

internal class MarshmallowCamera constructor(val context: Context) {

    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = manager.cameraIdList[0] ?: "0"
        } catch (ignored: Exception) {
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun toggleMarshmallowFlashlight(enable: Boolean) {
        try {
            manager.setTorchMode(cameraId!!, enable)
        } catch (e: Exception) {
            val mainRunnable = Runnable {
                EventBus.getDefault().post(Events.CameraUnavailable())
            }
            Handler(context.mainLooper).post(mainRunnable)
        }
    }
}
