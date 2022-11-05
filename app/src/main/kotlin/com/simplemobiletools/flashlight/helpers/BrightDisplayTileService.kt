package com.simplemobiletools.flashlight.helpers

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.simplemobiletools.flashlight.activities.BrightDisplayActivity

@TargetApi(Build.VERSION_CODES.N)
class BrightDisplayTileService : TileService() {

    override fun onClick() {
        val intent = Intent(applicationContext, BrightDisplayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }
}
