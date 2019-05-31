package zapsolutions.zap.util;

public class RefConstants {

    // This value has to be increased if any changes are made, that break the current implementation.
    // For example the hashing algorithm of the PIN. It will basically cause the app to reset itself on
    // next startup. HANDLE THIS WITH CARE. IT MIGHT CAUSE LOSS OF IMPORTANT DATA FOR USERS.
    public static final int CURRENT_SETTINGS_VER = 15;

    // If any changes are done here, CURRENT_SETTINGS_VER has to be updated.
    public static final int NUM_HASH_ITERATIONS = 5000;

    
    public static final int PIN_MIN_LENGTH = 4;
    public static final int PIN_MAX_LENGTH = 10;
    public static final int PIN_MAX_FAILS = 3;
    public static final int PIN_DELAY_TIME = 10;

    public static final int VIBRATE_SHORT = 50;
    public static final int VIBRATE_LONG = 200;

}
