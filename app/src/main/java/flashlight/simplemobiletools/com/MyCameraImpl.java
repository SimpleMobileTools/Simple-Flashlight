package flashlight.simplemobiletools.com;

public class MyCameraImpl {
    private android.hardware.Camera camera;
    private android.hardware.Camera.Parameters params;
    private boolean isFlashlightOn;
    private MyCamera callback;

    public MyCameraImpl(MyCamera camera) {
        callback = camera;
    }

    public void toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn;

        if (isFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void setupCamera() {
        if (camera == null) {
            camera = android.hardware.Camera.open();
            params = camera.getParameters();
            params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);

            if (isFlashlightOn)
                enableFlashlight();
        }
    }

    private void enableFlashlight() {
        params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        callback.enableFlashlight();
    }

    private void disableFlashlight() {
        params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
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
