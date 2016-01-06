package flashlight.simplemobiletools.com;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MyCamera {
    private ImageView toggleBtn;
    private MyCameraImpl cameraImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToggleButton();
        setupCameraImpl();
    }

    private void setupCameraImpl() {
        cameraImpl = new MyCameraImpl(this);
        cameraImpl.toggleFlashlight();
    }

    private void setupToggleButton() {
        toggleBtn = (ImageView) findViewById(R.id.toggle_btn);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraImpl.toggleFlashlight();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraImpl.setupCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraImpl.setupCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraImpl.releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraImpl.releaseCamera();
    }

    @Override
    public void enableFlashlight() {
        toggleBtn.setImageResource(R.mipmap.flashlight_big_on);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void disableFlashlight() {
        toggleBtn.setImageResource(R.mipmap.flashlight_big_off);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void cameraUnavailable() {
        final String errorMsg = getResources().getString(R.string.camera_error);
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        disableFlashlight();
    }
}
