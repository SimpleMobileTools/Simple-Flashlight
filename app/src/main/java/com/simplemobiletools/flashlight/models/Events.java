package com.simplemobiletools.flashlight.models;

public class Events {
    public static class StateChanged {
        private static boolean mIsEnabled;

        public StateChanged(boolean isEnabled) {
            mIsEnabled = isEnabled;
        }

        public boolean getIsEnabled() {
            return mIsEnabled;
        }
    }

    public static class CameraUnavailable {
    }
}
