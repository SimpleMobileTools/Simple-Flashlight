package com.simplemobiletools.flashlight;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class MarshmallowCamera {
    private static final String TAG = MyCameraImpl.class.getSimpleName();
    private MyCamera mCallback;
    private Context mContext;

    public MarshmallowCamera(MyCamera camera, Context cxt) {
        mCallback = camera;
        mContext = cxt;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void toggleMarshmallowFlashlight(boolean enable) {
        try {
            final CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            final String[] list = manager.getCameraIdList();
            manager.setTorchMode(list[0], enable);
        } catch (CameraAccessException e) {
            Log.e(TAG, "toggle marshmallow flashlight " + e.getMessage());
            mCallback.cameraUnavailable();
        }
    }
}
