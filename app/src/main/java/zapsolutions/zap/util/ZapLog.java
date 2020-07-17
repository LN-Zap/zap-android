package zapsolutions.zap.util;

import android.util.Log;

import zapsolutions.zap.BuildConfig;

/**
 * Use this class instead of the default log to prevent log messages in release builds.
 * As an additional "(ZapLog)"-TAG is always included, it is easy to just show logs
 * created from this class by using "ZapLog" as a filter.
 */
public class ZapLog {
    public static void v(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.v("(ZapLog) " + tag, message);
        }
    }

    public static void v(final String tag, String message, Throwable tr) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.v("(ZapLog) " + tag, message, tr);
        }
    }

    public static void d(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.d("(ZapLog) " + tag, message);
        }
    }

    public static void d(final String tag, String message, Throwable tr) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.d("(ZapLog) " + tag, message, tr);
        }
    }

    public static void i(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("(ZapLog) " + tag, message);
        }
    }

    public static void i(final String tag, String message, Throwable tr) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("(ZapLog) " + tag, message, tr);
        }
    }

    public static void w(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.w("(ZapLog) " + tag, message);
        }
    }

    public static void w(final String tag, String message, Throwable tr) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.w("(ZapLog) " + tag, message, tr);
        }
    }

    public static void e(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.e("(ZapLog) " + tag, message);
        }
    }

    public static void e(final String tag, String message, Throwable tr) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.e("(ZapLog) " + tag, message, tr);
        }
    }
}
