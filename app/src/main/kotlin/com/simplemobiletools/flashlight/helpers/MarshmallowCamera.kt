package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus

internal class MarshmallowCamera constructor(val context: Context) {

    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = manager.cameraIdList[0] ?: "0"
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun toggleMarshmallowFlashlight(enable: Boolean) {
        try {
            manager.setTorchMode(cameraId!!, enable)
        } catch (e: Exception) {
            context.showErrorToast(e)
            val mainRunnable = Runnable {
                EventBus.getDefault().post(Events.CameraUnavailable())
            }
            Handler(context.mainLooper).post(mainRunnable)
        }
    }
}
