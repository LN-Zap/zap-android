package zapsolutions.zap.setup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.BaseNodeConfig;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.RemoteConnectUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.nodesManagement.ManageNodesActivity;

public class ConnectRemoteNodeActivity extends BaseScannerActivity {

    public static final String EXTRA_STARTED_FROM_URI = "startedFromURI";

    private static final String LOG_TAG = ConnectRemoteNodeActivity.class.getName();
    private String mWalletUUID;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(ManageNodesActivity.NODE_ID)) {
                mWalletUUID = extras.getString(ManageNodesActivity.NODE_ID);
            }
            if (extras.getBoolean(EXTRA_STARTED_FROM_URI, false)) {
                String connectString = App.getAppContext().getUriSchemeData();
                App.getAppContext().setUriSchemeData(null);
                verifyDesiredConnection(connectString);
            }
        }

        showButtonHelp();
    }

    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        String clipboardContent = "";
        boolean isClipboardContentValid = false;
        try {
            clipboardContent = ClipBoardUtil.getPrimaryContent(getApplicationContext(), true);
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
    public void handleCameraResult(String result) {
        super.handleCameraResult(result);
        verifyDesiredConnection(result);
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
            public void onValidLndConnectString(BaseNodeConfig baseNodeConfig) {
                connectIfUserConfirms(baseNodeConfig);
            }

            @Override
            public void onValidBTCPayConnectData(BaseNodeConfig baseNodeConfig) {
                connectIfUserConfirms(baseNodeConfig);
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


    private void connectIfUserConfirms(BaseNodeConfig baseNodeConfig) {
        // Ask user to confirm the connection to remote host
        new UserGuardian(this, () -> {
            connect(baseNodeConfig);
        }).securityConnectToRemoteServer(baseNodeConfig.getHost());
    }

    private void connect(BaseNodeConfig baseNodeConfig) {
        // Connect using the supplied configuration
        RemoteConnectUtil.saveRemoteConfiguration(ConnectRemoteNodeActivity.this, baseNodeConfig, mWalletUUID, new RemoteConnectUtil.OnSaveRemoteConfigurationListener() {

            @Override
            public void onSaved(String id) {

                // The configuration was saved. Now make it the currently active wallet.
                PrefsUtil.editPrefs().putString(PrefsUtil.CURRENT_NODE_CONFIG, id).commit();

                // Do not ask for pin again...
                TimeOutUtil.getInstance().restartTimer();

                // In case another wallet was open before, we want to have all values reset.
                Wallet.getInstance().reset();

                // Show home screen, remove history stack. Going to HomeActivity will initiate the connection to our new remote configuration.
                Intent intent = new Intent(ConnectRemoteNodeActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onAlreadyExists() {
                new AlertDialog.Builder(ConnectRemoteNodeActivity.this)
                        .setMessage(R.string.node_already_exists)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }
        });
    }

}
