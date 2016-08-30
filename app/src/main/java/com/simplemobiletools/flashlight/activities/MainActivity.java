package com.simplemobiletools.flashlight.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

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
    @BindView(R.id.toggle_btn) ImageView mToggleBtn;
    @BindView(R.id.bright_display_btn) ImageView mBrightDisplayBtn;

    private static Bus mBus;
    private static MyCameraImpl mCameraImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBus = BusProvider.getInstance();
        changeIconColor(R.color.translucent_white, mBrightDisplayBtn);
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
        mCameraImpl.handleCameraSetup();
        mCameraImpl.checkFlashlight();

        if (mConfig.getBrightDisplay())
            mBrightDisplayBtn.setVisibility(View.VISIBLE);
        else
            mBrightDisplayBtn.setVisibility(View.GONE);
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
        if (mCameraImpl != null) {
            mCameraImpl.releaseCamera();
            mCameraImpl = null;
        }
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
    }

    public void disableFlashlight() {
        changeIconColor(R.color.translucent_white, mToggleBtn);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void changeIconColor(int colorId, ImageView imageView) {
        final int appColor = getResources().getColor(colorId);
        imageView.getDrawable().mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Subscribe
    public void cameraUnavailable(Events.CameraUnavailable event) {
        Utils.showToast(this, R.string.camera_error);
        disableFlashlight();
    }
}
