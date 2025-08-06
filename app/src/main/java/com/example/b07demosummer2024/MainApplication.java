package com.example.b07demosummer2024;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

public class MainApplication extends Application {
    private static final long BACKGROUND_TIMEOUT = 5000; // 5 seconds in milliseconds
    private static boolean requirePinAuth = false;
    private static long backgroundedTime = 0;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable backgroundTimer;

    public static void onBackground() {
        backgroundedTime = System.currentTimeMillis();
        backgroundTimer = () -> requirePinAuth = true;
        handler.postDelayed(backgroundTimer, BACKGROUND_TIMEOUT);
    }

    public static void onForeground() {
        if (backgroundTimer != null) {
            handler.removeCallbacks(backgroundTimer);
        }

        if (backgroundedTime > 0 &&
            System.currentTimeMillis() - backgroundedTime >= BACKGROUND_TIMEOUT) {
            requirePinAuth = true;
        }
    }

    public static boolean isRequirePinAuth() {
        return requirePinAuth;
    }

    public static void setPinAuthRequired(boolean required) {
        requirePinAuth = required;
    }
}
