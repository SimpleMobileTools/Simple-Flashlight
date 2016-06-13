package com.simplemobiletools.flashlight;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class MyCameraImpl {
    private static final String TAG = MyCameraImpl.class.getSimpleName();
    private Camera camera;
    private Camera.Parameters params;
    private boolean isFlashlightOn;
    private MyCamera callback;
    private Context context;

    public MyCameraImpl(MyCamera camera, Context cxt) {
        callback = camera;
        context = cxt;
        setupCamera();
    }

    public void toggleFlashlight() {
        setupCamera();
        isFlashlightOn = !isFlashlightOn;

        if (isFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void setupCamera() {
        if (isMarshmallow())
            return;

        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);

                if (isFlashlightOn)
                    enableFlashlight();
            } catch (Exception e) {
                Log.e(TAG, "setup camera " + e.getMessage());
                callback.cameraUnavailable();
            }
        }
    }

    private void enableFlashlight() {
        if (isMarshmallow()) {
            toggleMarshmallowFlashlight(true);
        } else {
            if (camera == null || params == null)
                return;

            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
        }
        callback.enableFlashlight();
    }

    private void disableFlashlight() {
        if (isMarshmallow()) {
            toggleMarshmallowFlashlight(false);
        } else {
            if (camera == null || params == null)
                return;

            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
        }
        callback.disableFlashlight();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void toggleMarshmallowFlashlight(boolean enable) {
        try {
            final CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            final String[] list = manager.getCameraIdList();
            manager.setTorchMode(list[0], enable);
        } catch (CameraAccessException e) {
            Log.e(TAG, "toggle marshmallow flashlight " + e.getMessage());
            callback.cameraUnavailable();
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private boolean isMarshmallow() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }
}
