package flashlight.simplemobiletools.com;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private Camera.Parameters params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupCamera();
    }

    private void setupCamera() {
        if (camera == null) {
            camera = Camera.open();
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }
}
