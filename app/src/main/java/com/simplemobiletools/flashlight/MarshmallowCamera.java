package com.simplemobiletools.flashlight;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import com.squareup.otto.Bus;

class MarshmallowCamera {
    private static final String TAG = MyCameraImpl.class.getSimpleName();

    private Context mContext;
    private CameraManager manager;
    private String cameraId;

    @TargetApi(Build.VERSION_CODES.M)
    MarshmallowCamera(Context cxt) {
        mContext = cxt;
        manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            final String[] list = manager.getCameraIdList();
            cameraId = list[0];
        } catch (CameraAccessException ignored) {
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    void toggleMarshmallowFlashlight(final Bus bus, boolean enable) {
        try {
            manager.setTorchMode(cameraId, enable);
        } catch (CameraAccessException e) {
            Log.e(TAG, "toggle marshmallow flashlight " + e.getMessage());
            bus.post(new Events.CameraUnavailable());
        }
    }
}
