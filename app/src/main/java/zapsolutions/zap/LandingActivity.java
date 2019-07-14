package zapsolutions.zap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import at.favre.lib.armadillo.Armadillo;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

public class LandingActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = "Landing Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // get the data from the URI Scheme
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            App.getAppContext().setUriSchemeData(uri.toString());
            ZapLog.debug(LOG_TAG, "URI was detected: " + uri.toString());
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int ver = prefs.getInt(PrefsUtil.SETTINGS_VER, 0);

        // support for clearing shared preferences,
        // in v1 we are now upgrading to encrypted shared pref
        // and thus need to reset all currently entered settings
        if (ver < RefConstants.CURRENT_SETTINGS_VER) {
            // Reset settings
            prefs.edit().clear().commit();
            // Reset connection settings
            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, PrefsUtil.PREFS_REMOTE)
                    .encryptionFingerprint(ctx)
                    .build();
            prefsRemote.edit().clear().commit();
            // Set new settings version
            prefs.edit().putInt(PrefsUtil.SETTINGS_VER, RefConstants.CURRENT_SETTINGS_VER).apply();
        }


        if (PrefsUtil.isWalletSetup()) {
            // Go to PIN entry screen
            Intent pinIntent = new Intent(this, PinEntryActivity.class);
            pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(pinIntent);
        } else {

            // Clear connection data if something is there

            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, PrefsUtil.PREFS_REMOTE)
                    .encryptionFingerprint(ctx)
                    .build();
            prefsRemote.edit().clear().commit();


            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }
    }
}
