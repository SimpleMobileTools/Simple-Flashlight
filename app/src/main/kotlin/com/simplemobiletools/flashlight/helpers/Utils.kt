package com.simplemobiletools.flashlight.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

object Utils {

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return mutableBitmap
    }
}
