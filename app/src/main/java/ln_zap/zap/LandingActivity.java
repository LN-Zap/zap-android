package ln_zap.zap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import at.favre.lib.armadillo.Armadillo;
import ln_zap.zap.baseClasses.App;
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
            // Reset settings
            prefs.edit().clear().commit();
            // Reset connection settings
            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, RefConstants.prefs_remote)
                    .encryptionFingerprint(ctx)
                    .build();
            prefsRemote.edit().clear().commit();
            // Set new settings version
            prefs.edit().putInt(RefConstants.settings_ver, RefConstants.currentSettingsVer).apply();
        }

        boolean isWalletSetup = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("isWalletSetup", false);

        if (isWalletSetup) {
            // Go to PIN entry screen
            Intent intent = new Intent(this, PinEntryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {

            // Clear connection data if something is there

            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, RefConstants.prefs_remote)
                    .encryptionFingerprint(ctx)
                    .build();
            prefsRemote.edit().clear().commit();


            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }
}
