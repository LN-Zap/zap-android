package zapsolutions.zap.setup;

import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
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
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.UtilFunctions;
import zapsolutions.zap.util.ZapLog;

public class ConnectRemoteNodeActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = "Connect to remote node activity";

    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;
    private UserGuardian mUG;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_connect);
        setupToolbar();

        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);

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
                connect(remoteConfiguration);
            }
        });
        mUG.securityConnectToRemoteServer(remoteConfiguration.getHost());
    }

    private void connect(RemoteConfiguration config) {

        WalletConfigsManager walletConfigsManager = new WalletConfigsManager();

        if (config instanceof LndConnectConfig) {
            LndConnectConfig lndConfig = (LndConnectConfig) config;
            walletConfigsManager.saveWalletConfig(WalletConfigsManager.DEFAULT_WALLET_NAME,
                    "remote", lndConfig.getHost(), lndConfig.getPort(),
                    lndConfig.getCert(), lndConfig.getMacaroon());
        } else if (config instanceof BTCPayConfig) {
            BTCPayConfig btcPayConfig = (BTCPayConfig) config;
            walletConfigsManager.saveWalletConfig(WalletConfigsManager.DEFAULT_WALLET_NAME,
                    "remote", btcPayConfig.getHost(), btcPayConfig.getPort(),
                    null, btcPayConfig.getMacaroon());
        }

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

}
