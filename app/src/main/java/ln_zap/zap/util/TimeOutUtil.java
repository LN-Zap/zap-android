package ln_zap.zap.util;

public class TimeOutUtil {

    // 10 seconds time out
    private static long TIMEOUT_DELAY = 1000 * 10;

    private static long appClosed = 0L;
    private static TimeOutUtil instance = null;

    private TimeOutUtil() { ; }

    public static TimeOutUtil getInstance() {

        if(instance == null) {
            instance = new TimeOutUtil();
        }

        return instance;
    }

    public void startTimer() {
        appClosed = System.currentTimeMillis();
    }

    public boolean isTimedOut() {
        return (System.currentTimeMillis() - appClosed) > TIMEOUT_DELAY;
    }

    public void reset() { appClosed = 0L; }

}
