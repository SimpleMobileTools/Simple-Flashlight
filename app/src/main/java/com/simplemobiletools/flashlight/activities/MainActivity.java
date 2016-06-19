package com.simplemobiletools.flashlight.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.simplemobiletools.flashlight.MyCamera;
import com.simplemobiletools.flashlight.MyCameraImpl;
import com.simplemobiletools.flashlight.R;
import com.simplemobiletools.flashlight.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MyCamera {
    @BindView(R.id.toggle_btn) ImageView mToggleBtn;

    private static MyCameraImpl mCameraImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupCameraImpl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
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
        mCameraImpl = new MyCameraImpl(this, this);
        mCameraImpl.toggleFlashlight();
    }

    @OnClick(R.id.toggle_btn)
    public void toggleFlashlight() {
        mCameraImpl.toggleFlashlight();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraImpl.handleCameraSetup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraImpl.handleCameraSetup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraImpl.releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraImpl.releaseCamera();
    }

    @Override
    public void enableFlashlight() {
        final int appColor = getResources().getColor(R.color.colorPrimary);
        mToggleBtn.setImageResource(R.mipmap.flashlight_big);
        mToggleBtn.getDrawable().mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_IN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void disableFlashlight() {
        mToggleBtn.setImageResource(R.mipmap.flashlight_big);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void cameraUnavailable() {
        Utils.showToast(this, R.string.camera_error);
        disableFlashlight();
    }
}
