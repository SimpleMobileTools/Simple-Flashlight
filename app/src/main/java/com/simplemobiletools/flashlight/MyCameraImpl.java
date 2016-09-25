package com.simplemobiletools.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.squareup.otto.Bus;

public class MyCameraImpl {
    private static final String TAG = MyCameraImpl.class.getSimpleName();
    private static Camera mCamera;
    private static Camera.Parameters mParams;
    private static Bus mBus;
    private Context mContext;
    private MarshmallowCamera mMarshmallowCamera;

    private static boolean mIsFlashlightOn;
    private static boolean mIsMarshmallow;

    public MyCameraImpl(Context cxt) {
        mContext = cxt;
        mIsMarshmallow = isMarshmallow();

        if (mBus == null) {
            mBus = BusProvider.getInstance();
            mBus.register(this);
        }

        handleCameraSetup();
        checkFlashlight();
    }

    public void toggleFlashlight() {
        mIsFlashlightOn = !mIsFlashlightOn;
        handleCameraSetup();
    }

    public void handleCameraSetup() {
        if (mIsMarshmallow) {
            setupMarshmallowCamera();
        } else {
            setupCamera();
        }
        checkFlashlight();
    }

    private void setupMarshmallowCamera() {
        if (mMarshmallowCamera == null) {
            mMarshmallowCamera = new MarshmallowCamera(mContext);
        }
    }

    private void setupCamera() {
        if (mIsMarshmallow)
            return;

        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mParams = mCamera.getParameters();
                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParams);
            } catch (Exception e) {
                Log.e(TAG, "setup mCamera " + e.getMessage());
                mBus.post(new Events.CameraUnavailable());
            }
        }
    }

    public void checkFlashlight() {
        if (mIsFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void enableFlashlight() {
        mIsFlashlightOn = true;
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(true);
        } else {
            if (mCamera == null || mParams == null) {
                return;
            }

            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParams);
            mCamera.startPreview();
        }
        mBus.post(new Events.StateChanged(true));
    }

    private void disableFlashlight() {
        mIsFlashlightOn = false;
        if (isMarshmallow()) {
            toggleMarshmallowFlashlight(false);
        } else {
            if (mCamera == null || mParams == null) {
                return;
            }

            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParams);
        }
        mBus.post(new Events.StateChanged(false));
    }

    private void toggleMarshmallowFlashlight(boolean enable) {
        mMarshmallowCamera.toggleMarshmallowFlashlight(mBus, enable);
    }

    public void releaseCamera() {
        if (mIsFlashlightOn) {
            disableFlashlight();
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        if (mBus != null) {
            mBus.unregister(this);
        }
        mIsFlashlightOn = false;
    }

    private boolean isMarshmallow() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }
}
