package com.simplemobiletools.flashlight.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.simplemobiletools.flashlight.BusProvider;
import com.simplemobiletools.flashlight.Config;
import com.simplemobiletools.flashlight.Events;
import com.simplemobiletools.flashlight.MyCameraImpl;
import com.simplemobiletools.flashlight.R;
import com.simplemobiletools.flashlight.Utils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SimpleActivity {
    private static final int CAMERA_PERMISSION = 1;
    private static final int MAX_STROBO_DELAY = 2000;
    private static final int MIN_STROBO_DELAY = 30;

    @BindView(R.id.toggle_btn) ImageView mToggleBtn;
    @BindView(R.id.bright_display_btn) ImageView mBrightDisplayBtn;
    @BindView(R.id.stroboscope_btn) ImageView mStroboscopeBtn;
    @BindView(R.id.stroboscope_bar) SeekBar mStroboscopeBar;

    private static Bus mBus;
    private MyCameraImpl mCameraImpl;
    private boolean mJustGrantedPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBus = BusProvider.getInstance();
        changeIconColor(R.color.translucent_white, mBrightDisplayBtn);
        changeIconColor(R.color.translucent_white, mStroboscopeBtn);
        mStroboscopeBar.setMax(MAX_STROBO_DELAY - MIN_STROBO_DELAY);
        mStroboscopeBar.setProgress(mStroboscopeBar.getMax() / 2);

        mStroboscopeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                final int frequency = mStroboscopeBar.getMax() - progress + MIN_STROBO_DELAY;
                if (mCameraImpl != null)
                    mCameraImpl.setStroboFrequency(frequency);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupCameraImpl() {
        mCameraImpl = new MyCameraImpl(this);
        mCameraImpl.enableFlashlight();
    }

    @OnClick(R.id.toggle_btn)
    public void toggleFlashlight() {
        mCameraImpl.toggleFlashlight();
    }

    @OnClick(R.id.bright_display_btn)
    public void launchBrightDisplay() {
        startActivity(new Intent(getApplicationContext(), BrightDisplayActivity.class));
    }

    @OnClick(R.id.stroboscope_btn)
    public void tryToggleStroboscope() {
        toggleStroboscope();
    }

    private void toggleStroboscope() {
        // use the old Camera API for stroboscope, the new Camera Manager is way too slow
        if (isCameraPermissionGranted() || Utils.isNougat()) {
            if (mCameraImpl.toggleStroboscope()) {
                mStroboscopeBar.setVisibility(mStroboscopeBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                changeIconColor(mStroboscopeBar.getVisibility() == View.VISIBLE ? R.color.colorPrimary : R.color.translucent_white, mStroboscopeBtn);
            }
        } else {
            final String[] permissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION) {
            mJustGrantedPermission = true;
            if (isCameraPermissionGranted()) {
                toggleStroboscope();
            } else {
                Utils.showToast(getApplicationContext(), R.string.camera_permission);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);

        if (mCameraImpl == null) {
            setupCameraImpl();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mJustGrantedPermission) {
            mJustGrantedPermission = false;
            return;
        }
        mCameraImpl.handleCameraSetup();
        mCameraImpl.checkFlashlight();

        mBrightDisplayBtn.setVisibility(mConfig.getBrightDisplay() ? View.VISIBLE : View.GONE);
        mStroboscopeBtn.setVisibility(mConfig.getStroboscope() ? View.VISIBLE : View.GONE);
        if (!mConfig.getStroboscope()) {
            mCameraImpl.stopStroboscope();
            mStroboscopeBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.newInstance(getApplicationContext()).setIsFirstRun(false);
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCameraImpl != null) {
            mCameraImpl.releaseCamera();
            mCameraImpl = null;
        }
    }

    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe
    public void stateChangedEvent(Events.StateChanged event) {
        if (event.getIsEnabled()) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void enableFlashlight() {
        changeIconColor(R.color.colorPrimary, mToggleBtn);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        changeIconColor(R.color.translucent_white, mStroboscopeBtn);
        mStroboscopeBar.setVisibility(View.INVISIBLE);
    }

    public void disableFlashlight() {
        changeIconColor(R.color.translucent_white, mToggleBtn);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void changeIconColor(int colorId, ImageView imageView) {
        final int appColor = getResources().getColor(colorId);
        imageView.getBackground().mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_IN);
    }

    @Subscribe
    public void cameraUnavailable(Events.CameraUnavailable event) {
        Utils.showToast(this, R.string.camera_error);
        disableFlashlight();
    }
}
