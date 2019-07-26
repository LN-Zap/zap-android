package zapsolutions.zap.setup;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfig;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfigParser;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectConfig;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectStringParser;
import zapsolutions.zap.util.PermissionsUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.ZapLog;

public class ConnectRemoteNodeActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = ConnectRemoteNodeActivity.class.getName();

    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;
    private UserGuardian mUG;
    private InputMethodManager mInputMethodManager;
    private String mWalletName = "";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_connect);
        setupToolbar();

        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mWalletName = extras.getString("walletAlias", "");
        }

        // Action when clicked on "paste"
        Button btnPaste = findViewById(R.id.scannerPaste);
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String clipboardContent = "";
                boolean isClipboardContentValid = false;
                try {
                    clipboardContent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                    isClipboardContentValid = true;
                } catch (NullPointerException e) {
                    showError(getResources().getString(R.string.error_emptyClipboardConnect), 4000);
                }
                if (isClipboardContentValid) {
                    verifyDesiredConnection(clipboardContent);
                }
            }
        });

        // Action when clicked on "help"
        Button btnHelp = findViewById(R.id.scannerHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ln-zap.github.io/zap-tutorials/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        // Check for camera permission
        if (PermissionsUtil.hasCameraPermission(ConnectRemoteNodeActivity.this)) {
            showCameraView();
        } else {
            PermissionsUtil.requestCameraPermission(ConnectRemoteNodeActivity.this, true);
        }
    }

    private void showCameraView() {
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(mScannerView);

        // Action when clicked on "flash button"
        mBtnFlashlight = findViewById(R.id.scannerFlashButton);
        mBtnFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScannerView.getFlash()) {
                    mScannerView.setFlash(false);
                    mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mGrayColor));
                } else {
                    mScannerView.setFlash(true);
                    mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mHighlightColor));
                }
            }
        });
    }

    private void verifyDesiredConnection(String connectString) {

        if (connectString.toLowerCase().startsWith("lndconnect")) {
            connectLndConnect(connectString);
        } else if (connectString.startsWith("config=")) {
            // URL to BTCPayConfigJson
            String configUrl = connectString.replace("config=", "");
            StringRequest btcPayConfigRequest = new StringRequest(Request.Method.GET, configUrl,
                    response -> connectBtcPay(response),
                    error -> showError(getResources().getString(R.string.error_unableToFetchBTCPayConfig), 4000));

            ZapLog.debug(LOG_TAG, "Fetching BTCPay config...");
            HttpClient.getInstance().addToRequestQueue(btcPayConfigRequest, "BTCPayConfigRequest");
        } else if (BTCPayConfigParser.isValidJson(connectString)) {
            // Valid BTCPay JSON
            connectBtcPay(connectString);
        } else {
            showError(getResources().getString(R.string.error_connection_unsupported_format), 7000);
        }
    }

    private void connectLndConnect(String connectString) {

        LndConnectStringParser parser = new LndConnectStringParser(connectString).parse();

        if (parser.hasError()) {
            switch (parser.getError()) {
                case LndConnectStringParser.ERROR_INVALID_CONNECT_STRING:
                    showError(getResources().getString(R.string.error_connection_invalidLndConnectString), 8000);
                    break;
                case LndConnectStringParser.ERROR_NO_MACAROON:
                    showError(getResources().getString(R.string.error_connection_no_macaroon), 5000);
                    break;
                case LndConnectStringParser.ERROR_INVALID_CERTIFICATE:
                    showError(getResources().getString(R.string.error_connection_invalid_certificate), 5000);
                    break;
                case LndConnectStringParser.ERROR_INVALID_MACAROON:
                    showError(getResources().getString(R.string.error_connection_invalid_macaroon), 5000);
                    break;
                case LndConnectStringParser.ERROR_INVALID_HOST_OR_PORT:
                    showError(getResources().getString(R.string.error_connection_invalid_host_or_port), 5000);
                    break;
            }
        } else {
            // Parsing was successful
            connectIfUserConfirms(parser.getConnectionConfig());
        }
    }

    private void connectBtcPay(@NonNull String btcPayConfigurationJson) {
        BTCPayConfigParser btcPayConfigParser = new BTCPayConfigParser(btcPayConfigurationJson).parse();

        if (btcPayConfigParser.hasError()) {
            switch (btcPayConfigParser.getError()) {
                case BTCPayConfigParser.ERROR_INVALID_JSON:
                    showError(getResources().getString(R.string.error_connection_btcpay_invalid_json), 4000);
                    break;
                case BTCPayConfigParser.ERROR_MISSING_BTC_GRPC_CONFIG:
                    showError(getResources().getString(R.string.error_connection_btcpay_invalid_config), 4000);
                    break;
                case BTCPayConfigParser.ERROR_NO_MACAROON:
                    showError(getResources().getString(R.string.error_connection_no_macaroon), 4000);
                    break;
            }
        } else {
            // Parsing was successful
            connectIfUserConfirms(btcPayConfigParser.getConnectionConfig());
        }
    }

    private void connectIfUserConfirms(RemoteConfiguration remoteConfiguration) {
        // Ask user to confirm the connection to remote host
        mUG = new UserGuardian(this, DialogName -> {
            if (UserGuardian.REMOTE_CONNECT.equals(DialogName)) {
                // Connect using the supplied configuration
                checkWalletName(remoteConfiguration);
            }
        });
        mUG.securityConnectToRemoteServer(remoteConfiguration.getHost());
    }

    private void checkWalletName(RemoteConfiguration remoteConfiguration) {
        if (WalletConfigsManager.getInstance().hasAtLeastOneConfig()) {
            if (mWalletName.equals("")) {
                showWalletNameInput(remoteConfiguration);
            } else {
                connect(remoteConfiguration);
            }
        } else {
            // We don't want to show the wallet name input for the first wallet that is created.
            mWalletName = WalletConfigsManager.DEFAULT_WALLET_NAME;
            connect(remoteConfiguration);

        }
    }

    private void connect(RemoteConfiguration config) {

        boolean success = false;

        WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();

        try {
            if (config instanceof LndConnectConfig) {
                LndConnectConfig lndConfig = (LndConnectConfig) config;

                walletConfigsManager.addWalletConfig(mWalletName,
                        WalletConfigsManager.WALLET_TYPE_REMOTE, lndConfig.getHost(), lndConfig.getPort(),
                        lndConfig.getCert(), lndConfig.getMacaroon());

                walletConfigsManager.apply();

                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mWalletName).commit();

                success = true;

            } else if (config instanceof BTCPayConfig) {
                BTCPayConfig btcPayConfig = (BTCPayConfig) config;

                walletConfigsManager.addWalletConfig(mWalletName,
                        WalletConfigsManager.WALLET_TYPE_REMOTE, btcPayConfig.getHost(), btcPayConfig.getPort(),
                        null, btcPayConfig.getMacaroon());

                walletConfigsManager.apply();

                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mWalletName).commit();

                success = true;

            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage(), 3000);
        }

        if (success) {
            // Do not ask for pin again...
            TimeOutUtil.getInstance().restartTimer();

            // We use commit here, as we want to be sure, that the data is saved and readable when we want to access it in the next step.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit()
                    .putBoolean("isWalletSetup", true)
                    .commit();

            // Show home screen, remove history stack
            Intent intent = new Intent(ConnectRemoteNodeActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    private void showError(String message, int duration) {
        Snackbar msg = Snackbar.make(findViewById(R.id.content_frame), message, Snackbar.LENGTH_LONG);
        View sbView = msg.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.superRed));
        msg.setDuration(duration);
        msg.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {

        String qrCodeContent = "";
        boolean isQrCodeContentValid = false;
        try {
            qrCodeContent = rawResult.getContents();
            isQrCodeContentValid = true;
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_qr_code_result_null), 4000);
        }

        if (isQrCodeContentValid) {
            verifyDesiredConnection(qrCodeContent);
        }


        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(ConnectRemoteNodeActivity.this);
            }
        }, 2000);
    }


    // Handle users permission choice
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.CAMERA_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, show the camera view.
                    showCameraView();
                } else {
                    // Permission denied, show required permission message.
                    mTvPermissionRequired.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showWalletNameInput(RemoteConfiguration remoteConfiguration) {
        // Show unlock dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Wallet Name");
        adb.setCancelable(false);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null, false);

        final EditText input = viewInflated.findViewById(R.id.input);
        input.setShowSoftInputOnFocus(true);
        input.requestFocus();


        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        adb.setView(viewInflated);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (WalletConfigsManager.getInstance().doesWalletConfigExist(input.getText().toString())) {
                    Toast.makeText(ConnectRemoteNodeActivity.this, "This name already exists.", Toast.LENGTH_LONG).show();
                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    showWalletNameInput(remoteConfiguration);
                } else {
                    mWalletName = input.getText().toString();
                    connect(remoteConfiguration);
                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                }
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dialog.cancel();
            }
        });

        adb.show();

    }

}
