package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@TargetApi(Build.VERSION_CODES.N)
class MyTileService : TileService() {

    override fun onClick() {
        MyCameraImpl.newInstance(this).toggleFlashlight()
        updateTile()
    }

    override fun onTileRemoved() {
        if (MyCameraImpl.isFlashlightOn)
            MyCameraImpl.newInstance(this).toggleFlashlight()
    }

    override fun onStartListening() {
        updateTile()
    }

    override fun onTileAdded() {
        updateTile()
    }

    private fun updateTile() {
        qsTile?.state = if (MyCameraImpl.isFlashlightOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }
}
