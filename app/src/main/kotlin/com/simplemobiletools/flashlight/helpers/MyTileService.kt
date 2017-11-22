package com.simplemobiletools.flashlight.helpers

import android.os.Build
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class MyTileService : TileService() {
    override fun onClick() {
        MyCameraImpl.newInstance(this).toggleFlashlight()
    }
}