package zapsolutions.zap.setup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.RemoteConnectUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;

public class ConnectRemoteNodeActivity extends BaseScannerActivity {
    private static final String LOG_TAG = ConnectRemoteNodeActivity.class.getName();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        showButtonHelp();

        showCameraWithPermissionRequest();
    }

    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        String clipboardContent = "";
        boolean isClipboardContentValid = false;
        try {
            clipboardContent = ClipBoardUtil.getPrimaryContent(getApplicationContext());
            isClipboardContentValid = true;
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardConnect), RefConstants.ERROR_DURATION_SHORT);
        }
        if (isClipboardContentValid) {
            verifyDesiredConnection(clipboardContent);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(ConnectRemoteNodeActivity.this, R.string.help_dialog_scanConnectionInfo);
    }

    @Override
    public void handleCameraResult(Result rawResult) {
        super.handleCameraResult(rawResult);

        String qrCodeContent = "";
        boolean isQrCodeContentValid = false;
        try {
            qrCodeContent = rawResult.getContents();
            isQrCodeContentValid = true;
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_qr_code_result_null), RefConstants.ERROR_DURATION_SHORT);
        }

        if (isQrCodeContentValid) {
            verifyDesiredConnection(qrCodeContent);
        }
    }

    @Override
    public void onButtonHelpClick() {
        super.onButtonHelpClick();

        String url = RefConstants.URL_HELP;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void verifyDesiredConnection(String connectString) {

        RemoteConnectUtil.decodeConnectionString(this, connectString, new RemoteConnectUtil.OnRemoteConnectDecodedListener() {
            @Override
            public void onValidLndConnectString(RemoteConfiguration remoteConfiguration) {
                connectIfUserConfirms(remoteConfiguration);
            }

            @Override
            public void onValidBTCPayConnectData(RemoteConfiguration remoteConfiguration) {
                connectIfUserConfirms(remoteConfiguration);
            }

            @Override
            public void onNoConnectData() {
                showError(getResources().getString(R.string.error_connection_unsupported_format), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }
        });
    }


    private void connectIfUserConfirms(RemoteConfiguration remoteConfiguration) {
        // Ask user to confirm the connection to remote host
        new UserGuardian(this, () -> {
            connect(remoteConfiguration);
        }).securityConnectToRemoteServer(remoteConfiguration.getHost());
    }

    private void connect(RemoteConfiguration remoteConfiguration) {
        // Connect using the supplied configuration
        RemoteConnectUtil.saveRemoteConfiguration(remoteConfiguration, new RemoteConnectUtil.OnSaveRemoteConfigurationListener() {

            @Override
            public void onSaved(String id) {

                // The configuration was saved. Now make it the currently active wallet.
                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, id).commit();

                // Do not ask for pin again...
                TimeOutUtil.getInstance().restartTimer();

                // We use commit here, as we want to be sure, that the data is saved and readable when we want to access it in the next step.
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConnectRemoteNodeActivity.this);
                prefs.edit()
                        .putBoolean(PrefsUtil.IS_WALLET_SETUP, true)
                        .commit();

                // In case another wallet was open before, we want to have all values reset.
                Wallet.getInstance().reset();

                // Show home screen, remove history stack. Going to HomeActivity will initiate the connection to our new remote configuration.
                Intent intent = new Intent(ConnectRemoteNodeActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }
        });
    }

}
