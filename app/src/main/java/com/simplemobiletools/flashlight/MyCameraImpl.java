package com.simplemobiletools.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

public class MyCameraImpl {
    private static final String TAG = MyCameraImpl.class.getSimpleName();
    private static Camera mCamera;
    private static Camera.Parameters mParams;
    private static MyCamera mCallback;
    private static Context mContext;
    private static MarshmallowCamera mMarshmallowCamera;

    private static boolean mIsFlashlightOn;
    private static boolean mIsMarshmallow;

    public MyCameraImpl(MyCamera camera, Context cxt) {
        mCallback = camera;
        mContext = cxt;
        mIsMarshmallow = isMarshmallow();
        handleCameraSetup();
    }

    public void toggleFlashlight() {
        handleCameraSetup();
        mIsFlashlightOn = !mIsFlashlightOn;

        if (mIsFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void handleCameraSetup() {
        if (mIsMarshmallow) {
            setupMarshmallowCamera();
        } else {
            setupCamera();
        }
    }

    private void setupMarshmallowCamera() {
        if (mMarshmallowCamera == null) {
            mMarshmallowCamera = new MarshmallowCamera(mCallback, mContext);
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

                if (mIsFlashlightOn)
                    enableFlashlight();
            } catch (Exception e) {
                Log.e(TAG, "setup mCamera " + e.getMessage());
                mCallback.cameraUnavailable();
            }
        }
    }

    private void enableFlashlight() {
        if (mIsMarshmallow) {
            toggleMarshmallowFlashlight(true);
        } else {
            if (mCamera == null || mParams == null) {
                return;
            }

            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParams);
        }
        mCallback.enableFlashlight();
    }

    private void disableFlashlight() {
        if (isMarshmallow()) {
            toggleMarshmallowFlashlight(false);
        } else {
            if (mCamera == null || mParams == null) {
                return;
            }

            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParams);
        }
        mCallback.disableFlashlight();
    }

    private void toggleMarshmallowFlashlight(boolean enable) {
        mMarshmallowCamera.toggleMarshmallowFlashlight(enable);
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean isMarshmallow() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }
}
