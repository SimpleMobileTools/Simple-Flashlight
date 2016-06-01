package com.simplemobiletools.flashlight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MyCamera {
    @BindView(R.id.toggle_btn) ImageView toggleBtn;
    private MyCameraImpl cameraImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupCameraImpl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                final Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupCameraImpl() {
        cameraImpl = new MyCameraImpl(this);
        cameraImpl.toggleFlashlight();
    }

    @OnClick(R.id.toggle_btn)
    public void toggleFlashlight() {
        cameraImpl.toggleFlashlight();
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
