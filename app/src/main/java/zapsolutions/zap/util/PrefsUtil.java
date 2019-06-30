package zapsolutions.zap.util;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.customView.OnChainFeeView;

/**
 * This class simplifies management of preferences.
 */
public class PrefsUtil {

    // shared preference references
    public static final String IS_WALLET_SETUP = "isWalletSetup";
    public static final String PREVENT_SCREEN_RECORDING = "preventScreenRecording";
    public static final String FIRST_CURRENCY_IS_PRIMARY = "firstCurrencyIsPrimary";
    public static final String PIN_HASH = "pin_hash";
    public static final String PIN_LENGTH = "pin_length";
    public static final String SETTINGS_VER = "settings_ver";
    public static final String ON_CHAIN_FEE_TIER = "on_chain_fee_tier";

    // remote node preferences references
    public static final String PREFS_REMOTE = "prefs_remote";
    public static final String REMOTE_COMBINED = "remote_combined";


    // Access to default shared prefs

    public static SharedPreferences getPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    public static SharedPreferences.Editor edit(){
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
    }


    // Shortcuts to often used preferences

    public static boolean isWalletSetup() {
        return getPrefs().getBoolean(IS_WALLET_SETUP, false);
    }

    public static boolean preventScreenRecording() {
        return getPrefs().getBoolean(PREVENT_SCREEN_RECORDING, true);
    }

    public static boolean firstCurrencyIsPrimary(){
        return getPrefs().getBoolean(FIRST_CURRENCY_IS_PRIMARY, true);
    }

    public static String getOnChainFeeTier() { return getPrefs().getString(ON_CHAIN_FEE_TIER, OnChainFeeView.OnChainFeeTier.FAST.name()); }
}
