package zapsolutions.zap;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.util.NfcUtil;
import zapsolutions.zap.util.PinScreenUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

public class LandingActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = LandingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save data when App was started with a task.
        if (PrefsUtil.isWalletSetup()) {

            // Zap was started from an URI link.
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                App.getAppContext().setUriSchemeData(uri.toString());
                ZapLog.debug(LOG_TAG, "URI was detected: " + uri.toString());
            }

            // Zap was started using NFC.
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                NfcUtil.readTag(LandingActivity.this, intent, payload -> App.getAppContext().setUriSchemeData(payload));
            }
        }


        // support for clearing shared preferences, on breaking changes
        if (PrefsUtil.getPrefs().contains(PrefsUtil.SETTINGS_VERSION)) {
            int ver = PrefsUtil.getPrefs().getInt(PrefsUtil.SETTINGS_VERSION, RefConstants.CURRENT_SETTINGS_VERSION);
            if (ver < RefConstants.CURRENT_SETTINGS_VERSION) {
                if (ver == 17) {
                    convertWalletNameToUUID();
                } else {
                    resetApp();
                }
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

    private void convertWalletNameToUUID() {
        if (PrefsUtil.isWalletSetup()) {
            if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                WalletConfig config = (WalletConfig)WalletConfigsManager.getInstance().getWalletConfigsJson().getConnections().toArray()[0];
                WalletConfigsManager.getInstance().removeAllWalletConfigs();
                String id = WalletConfigsManager.getInstance().addWalletConfig(config.getHost(), config.getType(), config.getHost(), config.getPort(), config.getCert(), config.getMacaroon()).getId();
                try {
                    WalletConfigsManager.getInstance().apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, id).commit();
                enterWallet();
            } else {
                enterWallet();
            }
        } else {
            enterWallet();
        }
    }

    private void enterWallet() {

        // Set new settings version
        PrefsUtil.edit().putInt(PrefsUtil.SETTINGS_VERSION, RefConstants.CURRENT_SETTINGS_VERSION).commit();

        if (PrefsUtil.isWalletSetup()) {
            PinScreenUtil.askForAccess(this, () -> {
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                // FinishAffinity is needed here as this forces the on destroy events from previous activities to be executed before continuing.
                finishAffinity();

                startActivity(homeIntent);
            });

        } else {

            // Clear connection data if something is there
            PrefsUtil.edit().remove(PrefsUtil.WALLET_CONFIGS).commit();


            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }
    }
}
