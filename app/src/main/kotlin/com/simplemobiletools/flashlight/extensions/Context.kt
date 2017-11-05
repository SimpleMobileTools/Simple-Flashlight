package com.simplemobiletools.flashlight.extensions

import android.content.Context
import com.simplemobiletools.flashlight.helpers.Config

val Context.config: Config get() = Config.newInstance(this)
