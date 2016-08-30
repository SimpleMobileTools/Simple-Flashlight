package com.simplemobiletools.flashlight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class Utils {
    public static Bitmap drawableToBitmap(Drawable drawable) {
        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();
        final Bitmap mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return mutableBitmap;
    }

    public static void showToast(Context context, int resId) {
        Toast.makeText(context, context.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }
}
