package ln_zap.zap.util;

import android.util.Log;
import ln_zap.zap.BuildConfig;

/**
 * Use this class instead of the default log to prevent log messages in release builds.
 * As an additional "(ZapLog)"-TAG is always included, it is easy to just show logs
 * created from this class by using "ZapLog" as a filter.
 */
public class ZapLog {
    public static void debug(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.d("(ZapLog) "+ tag, message);
        }
    }
}
