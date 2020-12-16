package com.simplemobiletools.flashlight

import android.app.Application
import com.simplemobiletools.commons.extensions.checkUseEnglish

class App : Application() {
    companion object {
        var flashlight_end_time: Long = 100000L
    }
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
