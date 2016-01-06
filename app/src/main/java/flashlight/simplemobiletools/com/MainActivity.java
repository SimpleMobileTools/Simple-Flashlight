package flashlight.simplemobiletools.com;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private Camera.Parameters params;
    private boolean isFlashlightOn;
    private ImageView toggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToggleButton();
        setupCamera();
        toggleFlashlight();
    }

    private void setupToggleButton() {
        toggleBtn = (ImageView) findViewById(R.id.toggle_btn);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlashlight();
            }
        });
    }

    private void toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn;

        if (isFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    private void setupCamera() {
        if (camera == null) {
            camera = Camera.open();
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);

            if (isFlashlightOn)
                enableFlashlight();
        }
    }

    private void enableFlashlight() {
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        toggleBtn.setBackground(getResources().getDrawable(R.mipmap.flashlight_big_on));
    }

    private void disableFlashlight() {
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        toggleBtn.setBackground(getResources().getDrawable(R.mipmap.flashlight_big_off));
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
