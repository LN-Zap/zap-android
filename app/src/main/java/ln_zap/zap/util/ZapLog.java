package ln_zap.zap.util;

import android.util.Log;
import ln_zap.zap.BuildConfig;

// this class is used instead of normal log to prevent log messages in release builds
public class ZapLog {
    public static void debug(final String tag, String message) {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.d("(ZapLog) "+ tag, message);
        }
    }
}
