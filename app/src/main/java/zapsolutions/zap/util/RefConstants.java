package zapsolutions.zap.util;

public class RefConstants {

    // This value has to be increased if any changes are made, that break the current implementation.
    // For example the hashing algorithm of the PIN. It will basically cause the app to reset itself on
    // next startup. HANDLE THIS WITH CARE. IT MIGHT CAUSE LOSS OF IMPORTANT DATA FOR USERS.
    public static final int currentSettingsVer = 15;

}
