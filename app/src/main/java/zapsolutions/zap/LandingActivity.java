package zapsolutions.zap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import at.favre.lib.armadillo.Armadillo;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.util.PrefsUtil;
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


        if (PrefsUtil.isWalletSetup()) {
            // Go to PIN entry screen
            Intent pinIntent = new Intent(this, PinEntryActivity.class);
            pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(pinIntent);
        } else {

            // Clear connection data if something is there
            PrefsUtil.edit().putString(PrefsUtil.WALLET_CONFIGS, "").commit();

            // ToDo: Remove this when nobody has the old version installed.
            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, PrefsUtil.PREFS_ENCRYPTED)
                    .encryptionFingerprint(ctx)
                    .build();
            prefsRemote.edit().clear().commit();
            // ToDO End


            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }
    }
}
