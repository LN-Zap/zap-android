package zapsolutions.zap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.pin.PinEntryActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

public class LandingActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = LandingActivity.class.getName();

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


        // support for clearing shared preferences, on breaking changes
        if (PrefsUtil.getPrefs().contains(PrefsUtil.SETTINGS_VERSION)) {
            int ver = PrefsUtil.getPrefs().getInt(PrefsUtil.SETTINGS_VERSION, RefConstants.CURRENT_SETTINGS_VERSION);
            if (ver < RefConstants.CURRENT_SETTINGS_VERSION) {
                resetApp();
            } else {
                enterWallet();
            }
        } else {
            // Make sure settings get reset for versions that don't expose settings version
            resetApp();
        }

    }

    private void resetApp() {
        if (PrefsUtil.isWalletSetup()) {
            // Reset settings
            PrefsUtil.edit().clear().commit();

            new AlertDialog.Builder(LandingActivity.this)
                    .setTitle(R.string.app_reset_title)
                    .setMessage(R.string.app_reset_message)
                    .setCancelable(true)
                    .setOnCancelListener(dialogInterface -> enterWallet())
                    .setPositiveButton(R.string.ok, (dialog, whichButton) -> enterWallet())
                    .show();
        } else {
            enterWallet();
        }
    }

    private void enterWallet() {

        // Set new settings version
        PrefsUtil.edit().putInt(PrefsUtil.SETTINGS_VERSION, RefConstants.CURRENT_SETTINGS_VERSION).commit();

        if (PrefsUtil.isWalletSetup()) {

            if (PrefsUtil.isPinEnabled()){
                // Go to PIN entry screen
                Intent pinIntent = new Intent(this, PinEntryActivity.class);
                pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(pinIntent);
            } else {

                // Check if pin is active according to key store
                boolean isPinActive = false;
                try {
                    isPinActive =  new Cryptography(this).isPinActive();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Only allow access if pin is not active in key store!
                if (isPinActive) {
                    // According to the key store, the pin is still active. This happens if the pin got deleted from the prefs without also removing the keystore entry.
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.error_pin_deactivation_attempt)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    LandingActivity.this.finish();
                                }
                            }).show();
                } else {
                    Intent homeIntent = new Intent(this, HomeActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                }
            }
        } else {

            // Clear connection data if something is there
            PrefsUtil.edit().putString(PrefsUtil.WALLET_CONFIGS, "").commit();


            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }
    }
}
