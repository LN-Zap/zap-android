package zapsolutions.zap.util;

import java.util.concurrent.TimeUnit;

public class RefConstants {

    /* This value has to be increased if any changes are made, that break the current implementation.
    It should ONLY be UPDATED ON BREAKING CHANGES.
    For example the hashing algorithm of the PIN. It will basically cause the app to reset itself on
    next startup. HANDLE THIS WITH CARE. IT MIGHT CAUSE LOSS OF IMPORTANT DATA FOR USERS.

    History:
    16: Biometrics and new Cryptography using Android Keystore
    17: Removed deviceID usage
    18: Wallet name based WalletConfigs -> UUID based WalletConfigs
    */
    public static final int CURRENT_SETTINGS_VERSION = 18;

    // If any changes are done here, CURRENT_SETTINGS_VERSION has to be updated.
    public static final int NUM_HASH_ITERATIONS = 5000;

    ///////////////////////////////////////////////////////////////////////////////////
    // All settings below here do not require an update of the CURRENT_SETTINGS_VERSION
    ///////////////////////////////////////////////////////////////////////////////////

    // PIN settings
    public static final int PIN_MIN_LENGTH = 4;
    public static final int PIN_MAX_LENGTH = 10;
    public static final int PIN_MAX_FAILS = 3;
    public static final int PIN_DELAY_TIME = 30;

    // API request timeouts
    public static final int TIMEOUT_SHORT = 5;
    public static final int TIMEOUT_MEDIUM = 10;
    public static final int TIMEOUT_LONG = 15;

    // Number of seconds after moving the app to background until the app gets locked.
    public static final int ACCESS_TIMEOUT = 10;

    // Schedule intervals
    public static final int EXCHANGE_RATE_PERIOD = 3;
    public static final TimeUnit EXCHANGE_RATE_PERIOD_UNIT = TimeUnit.MINUTES;

    // Haptic vibration
    public static final int VIBRATE_SHORT = 50;
    public static final int VIBRATE_LONG = 200;

    /* This value is a threshold used to determine if the user specified fee limit should
    be taken into consideration.
    If the payment amount is below/equal to this threshold, the user setting is not used.
    If the payment amount is above this threshold, the user setting will be considered. */
    public static final int LN_PAYMENT_FEE_THRESHOLD = 100;

    // URLS
    public static final String URL_HELP = "https://docs.zaphq.io/";
    public static final String URL_SUGGESTED_NODES = "https://resources.zaphq.io/api/v1/suggested-nodes";

    // Other
    public static final String SETUP_MODE = "setupMode";

}
