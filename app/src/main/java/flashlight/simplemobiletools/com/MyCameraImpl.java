package flashlight.simplemobiletools.com;

import android.hardware.Camera;

public class MyCameraImpl {
    private Camera camera;
    private Camera.Parameters params;
    private boolean isFlashlightOn;
    private MyCamera callback;

    public MyCameraImpl(MyCamera camera) {
        callback = camera;
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
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);

                if (isFlashlightOn)
                    enableFlashlight();
            } catch (Exception e) {
                callback.cameraUnavailable();
            }
        }
    }

    private void enableFlashlight() {
        if (camera == null || params == null)
            return;

        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        callback.enableFlashlight();
    }

    private void disableFlashlight() {
        if (camera == null || params == null)
            return;

        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        callback.disableFlashlight();
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
