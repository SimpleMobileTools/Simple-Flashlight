package com.simplemobiletools.flashlight.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

class ShutDownReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        exitProcess(0)
    }
}
