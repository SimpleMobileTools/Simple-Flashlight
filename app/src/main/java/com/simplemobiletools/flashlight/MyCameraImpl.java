package com.simplemobiletools.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

public class MyCameraImpl {
    private static final String TAG = MyCameraImpl.class.getSimpleName();

    private static Camera mCamera;
    private static Camera.Parameters mParams;
    private static Bus mBus;
    private Context mContext;
    private MarshmallowCamera mMarshmallowCamera;
    private volatile boolean mShouldStroboscopeStop;
    private volatile boolean mIsStroboscopeRunning;

    private static boolean mIsFlashlightOn;
    private static boolean mIsMarshmallow;
    private static boolean mShouldEnableFlashlight;
    private static int mStroboFrequency;

    public MyCameraImpl(Context cxt) {
        mContext = cxt;
        mIsMarshmallow = isMarshmallow();
        mStroboFrequency = 1000;

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

    public void setStroboFrequency(int frequency) {
        mStroboFrequency = frequency;
    }

    public boolean toggleStroboscope() {
        if (!mIsStroboscopeRunning)
            disableFlashlight();

        if (mCamera == null) {
            initCamera();
        }

        if (mCamera == null) {
            Utils.showToast(mContext, R.string.camera_error);
            return false;
        }

        if (mIsStroboscopeRunning) {
            stopStroboscope();
        } else {
            new Thread(stroboscope).start();
        }
        return true;
    }

    public void stopStroboscope() {
        mShouldStroboscopeStop = true;
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
            initCamera();
        }
    }

    private void initCamera() {
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

    public void checkFlashlight() {
        if (mIsFlashlightOn) {
            enableFlashlight();
        } else {
            disableFlashlight();
        }
    }

    public void enableFlashlight() {
        mShouldStroboscopeStop = true;
        if (mIsStroboscopeRunning) {
            mShouldEnableFlashlight = true;
            return;
        }

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

        Runnable mainRunnable = new Runnable() {
            @Override
            public void run() {
                mBus.post(new Events.StateChanged(true));
            }
        };
        new Handler(mContext.getMainLooper()).post(mainRunnable);
    }

    private void disableFlashlight() {
        if (mIsStroboscopeRunning) {
            return;
        }

        mIsFlashlightOn = false;
        if (mIsMarshmallow) {
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

    private Runnable stroboscope = new Runnable() {
        @Override
        public void run() {
            if (mIsStroboscopeRunning) {
                return;
            }

            mShouldStroboscopeStop = false;
            mIsStroboscopeRunning = true;

            if (mCamera == null) {
                initCamera();
            }

            Camera.Parameters torchOn = mCamera.getParameters();
            Camera.Parameters torchOff = mCamera.getParameters();
            torchOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            torchOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

            while (!mShouldStroboscopeStop) {
                try {
                    mCamera.setParameters(torchOn);
                    Thread.sleep(mStroboFrequency);
                    mCamera.setParameters(torchOff);
                    Thread.sleep(mStroboFrequency);
                } catch (InterruptedException ignored) {
                    mShouldStroboscopeStop = true;
                } catch (RuntimeException ignored) {
                    mShouldStroboscopeStop = true;
                }
            }

            try {
                if (mCamera != null) {
                    mCamera.setParameters(torchOff);
                    if (!mShouldEnableFlashlight || mIsMarshmallow) {
                        mCamera.release();
                        mCamera = null;
                    }
                }
            } catch (RuntimeException ignored) {

            }

            mIsStroboscopeRunning = false;
            mShouldStroboscopeStop = false;

            if (mShouldEnableFlashlight) {
                enableFlashlight();
                mShouldEnableFlashlight = false;
            }
        }
    };
}
