package flashlight.simplemobiletools.com;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

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
        cameraImpl.setupCamera();
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
        toggleBtn.setBackground(getResources().getDrawable(R.mipmap.flashlight_big_on));
    }

    @Override
    public void disableFlashlight() {
        toggleBtn.setBackground(getResources().getDrawable(R.mipmap.flashlight_big_off));
    }
}
