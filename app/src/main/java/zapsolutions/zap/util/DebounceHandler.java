package zapsolutions.zap.util;

import android.os.Handler;

public class DebounceHandler {

    static int DEBOUNCE_1_SECOND = 1000;

    private Handler mHandler;

    public DebounceHandler() {
        mHandler = new Handler();
    }

    /**
     * Will execute runnable after debounceDelay, or if new attempt
     * within delay, will remove old runnable and start new delay.
     *
     * @param runnable      the runnable to execute
     * @param debounceDelay the delay to wait for execution
     */
    public void attempt(Runnable runnable, int debounceDelay) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(runnable, debounceDelay);
    }

    public void shutdown() {
        mHandler.removeCallbacksAndMessages(null);
    }
}
