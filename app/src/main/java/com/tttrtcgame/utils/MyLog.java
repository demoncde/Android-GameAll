package com.tttrtcgame.utils;


import android.util.Log;

/**
 * Created by wangzhiguo on 17/10/31.
 */

public class MyLog {
    private static boolean IS_DEBUG = true;
    public static final String TAG = "WSTECH";

    public static void i(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG , msg);
    }

    public static void e(String msg) {
        Log.e(TAG, "[V2-TECH-ERROR]" + msg);
    }
}
