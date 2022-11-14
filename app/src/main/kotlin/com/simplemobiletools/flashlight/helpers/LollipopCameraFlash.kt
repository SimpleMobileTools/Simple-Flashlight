@file:Suppress("DEPRECATION")

package com.simplemobiletools.flashlight.helpers

import android.graphics.SurfaceTexture
import android.hardware.Camera

class LollipopCameraFlash : CameraFlash {
    private var camera: Camera? = null
    private var params: Camera.Parameters? = null

    override fun toggleFlashlight(enable: Boolean) {
        if (camera == null || params == null || camera!!.parameters == null) {
            return
        }
        val flashMode = if (enable) Camera.Parameters.FLASH_MODE_ON else Camera.Parameters.FLASH_MODE_OFF
        params!!.flashMode = flashMode
        camera!!.parameters = params
        if (enable) {
            val dummy = SurfaceTexture(1)
            camera!!.setPreviewTexture(dummy)
            camera!!.startPreview()
        }
    }

    override fun initialize() {
        camera = Camera.open()
        params = camera!!.parameters
        params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera!!.parameters = params
    }

    override fun release() {
        camera!!.release()
        camera = null
    }
}
