package com.simplemobiletools.flashlight.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toggle_btn) ImageView mToggleBtn;

    private static Bus mBus;
    private static MyCameraImpl mCameraImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBus = BusProvider.getInstance();
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
        changeIconColor(R.color.colorPrimary);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void disableFlashlight() {
        changeIconColor(R.color.translucent_white);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void changeIconColor(int colorId) {
        final int appColor = getResources().getColor(colorId);
        mToggleBtn.getDrawable().mutate().setColorFilter(appColor, PorterDuff.Mode.SRC_IN);
    }

    @Subscribe
    public void cameraUnavailable(Events.CameraUnavailable event) {
        Utils.showToast(this, R.string.camera_error);
        disableFlashlight();
    }
}
