package com.simplemobiletools.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

public class MyCameraImpl {
    private static final String TAG = MyCameraImpl.class.getSimpleName();
    private Camera camera;
    private Camera.Parameters params;
    private boolean isFlashlightOn;
    private MyCamera callback;
    private Context context;
    private boolean isMarshmallow;
    private MarshmallowCamera marshmallowCamera;

    public MyCameraImpl(MyCamera camera, Context cxt) {
        callback = camera;
        context = cxt;
        isMarshmallow = isMarshmallow();
        handleCameraSetup();
    }

    public void toggleFlashlight() {
        handleCameraSetup();
        isFlashlightOn = !isFlashlightOn;

        if (isFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void handleCameraSetup() {
        if (isMarshmallow) {
            setupMarshmallowCamera();
        } else {
            setupCamera();
        }
    }

    private void setupMarshmallowCamera() {
        if (marshmallowCamera == null) {
            marshmallowCamera = new MarshmallowCamera(callback, context);
        }
    }

    private void setupCamera() {
        if (isMarshmallow)
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
        if (isMarshmallow) {
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

    private void toggleMarshmallowFlashlight(boolean enable) {
        marshmallowCamera.toggleMarshmallowFlashlight(enable);
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
