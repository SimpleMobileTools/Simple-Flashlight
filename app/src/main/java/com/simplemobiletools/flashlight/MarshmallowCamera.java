package com.simplemobiletools.flashlight;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import com.squareup.otto.Bus;

public class MarshmallowCamera {
    private static final String TAG = MyCameraImpl.class.getSimpleName();

    private Context mContext;

    public MarshmallowCamera(Context cxt) {
        mContext = cxt;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void toggleMarshmallowFlashlight(final Bus bus, boolean enable) {
        try {
            final CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            final String[] list = manager.getCameraIdList();
            if (list.length > 0)
                manager.setTorchMode(list[0], enable);
        } catch (CameraAccessException e) {
            Log.e(TAG, "toggle marshmallow flashlight " + e.getMessage());
            bus.post(new Events.CameraUnavailable());
        }
    }
}
