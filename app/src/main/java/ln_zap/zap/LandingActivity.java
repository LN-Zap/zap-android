package ln_zap.zap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.preference.PreferenceManager;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;

public class LandingActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int ver = prefs.getInt(RefConstants.settings_ver, 0);

        // support for clearing shared preferences,
        // in v1 we are now upgrading to encrypted shared pref
        // and thus need to reset all currently entered settings
        if (ver < RefConstants.currentSettingsVer) {
            prefs.edit().clear().commit();
            prefs.edit().putInt(RefConstants.settings_ver, RefConstants.currentSettingsVer).apply();
        }

        boolean isWalletSetup = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("isWalletSetup",false);

        if (isWalletSetup){
            // Go to PIN entry screen
            Intent intent = new Intent(this, PinEntryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else{
            // Go to welcome screen
            Intent intent = new Intent(this, WelcomeNewUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
}
