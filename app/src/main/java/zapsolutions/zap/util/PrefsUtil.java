package zapsolutions.zap.util;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import zapsolutions.zap.baseClasses.App;

/**
 * This class simplifies management of preferences.
 */
public class PrefsUtil {

    // shared preference references
    public static final String isWalletSetup = "isWalletSetup";
    public static final String preventScreenRecording = "preventScreenRecording";
    public static final String pin_hash = "pin_hash";
    public static final String pin_length = "pin_length";
    public static final String settings_ver = "settings_ver";

    // remote node preferences references
    public static final String prefs_remote = "prefs_remote";
    public static final String remote_combined = "remote_combined";


    // Access to default shared prefs

    public static SharedPreferences getPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    public static SharedPreferences.Editor edit(){
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
    }


    // Shortcuts to often used preferences

    public static boolean isWalletSetup() {
        return getPrefs().getBoolean(isWalletSetup, false);
    }

    public static boolean preventScreenRecording() {
        return getPrefs().getBoolean(preventScreenRecording, true);
    }

    public static boolean firstCurrencyIsPrimary(){
        return getPrefs().getBoolean("firstCurrencyIsPrimary", true);
    }
}
