package ln_zap.zap.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * When the calling thread has a Looper installed (like the UI thread), an instance of ExecuteOnCaller will submit
 * Runnables into the caller thread. Otherwise it will submit the Runnables to the UI thread.
 */
public class ExecuteOnCaller implements Executor {

    private static ThreadLocal<Handler> threadLocalHandler = new ThreadLocal<Handler>() {
        @Override
        protected Handler initialValue() {
            Looper looper = Looper.myLooper();
            if (looper == null)
                looper = Looper.getMainLooper();
            return new Handler(looper);
        }
    };

    private final Handler handler = threadLocalHandler.get();

    @Override
    public void execute(Runnable command) {
        handler.post(command);
    }
}
