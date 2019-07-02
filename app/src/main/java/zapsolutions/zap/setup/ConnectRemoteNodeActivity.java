package zapsolutions.zap.setup;

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
import android.content.ClipboardManager;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.io.BaseEncoding;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.CustomSSLSocketFactory;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.util.PermissionsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UtilFunctions;
import zapsolutions.zap.util.ZapLog;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ConnectRemoteNodeActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = "Connect to remote node activity";

    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;


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

            // Parse lndconnect string

            URI connectURI = null;
            try {
                connectURI = new URI(connectString);
                if (!connectURI.getScheme().equals("lndconnect")) {
                    showError(getResources().getString(R.string.error_connection_invalidLndConnectString), 8000);
                } else {

                    String cert = null;
                    String macaroon = null;

                    // Fetch params
                    if (connectURI.getQuery() != null) {
                        String[] valuePairs = connectURI.getQuery().split("&");

                        boolean validParams = true;

                        for (String pair : valuePairs) {
                            String[] param = pair.split("=");
                            if (param.length > 1) {
                                if (param[0].equals("cert")) {
                                    cert = param[1];
                                }
                                if (param[0].equals("macaroon")) {
                                    macaroon = param[1];
                                }
                            }
                        }

                        // validate cert (Certificate is not mandatory for BTCPay server for example, therefore null is valid)
                        if (cert != null){
                            try {
                                byte[] certificateBytes = BaseEncoding.base64Url().decode(cert);
                                try {
                                    CustomSSLSocketFactory.create(certificateBytes);
                                } catch (RuntimeException e){
                                    validParams = false;
                                    ZapLog.debug(LOG_TAG, "certificate creation failed");
                                    showError(getResources().getString(R.string.error_connection_invalid_certificate), 5000);
                                }
                            } catch (IllegalArgumentException e) {
                                validParams = false;
                                ZapLog.debug(LOG_TAG, "cert decoding failed");
                                showError(getResources().getString(R.string.error_connection_invalid_certificate), 5000);
                            }
                        }

                        // validate macaroon if everything was valid so far
                        if (validParams) {
                            if (macaroon == null) {
                                validParams = false;
                                ZapLog.debug(LOG_TAG, "lnd connect string does not include a macaroon");
                                showError(getResources().getString(R.string.error_connection_no_macaroon), 5000);
                            } else {
                                try {
                                    BaseEncoding.base64Url().decode(macaroon);
                                } catch (IllegalArgumentException e) {
                                    validParams = false;
                                    ZapLog.debug(LOG_TAG, "macaroon decoding failed");
                                    showError(getResources().getString(R.string.error_connection_invalid_macaroon), 5000);
                                }
                            }
                        }

                        if (validParams) {
                            // Everything is ok, initiate connection
                            connect(connectURI.getHost(), connectURI.getPort(), cert, macaroon);
                        }

                    } else {
                        ZapLog.debug(LOG_TAG, "Connect URI has no parameters");
                        showError(getResources().getString(R.string.error_connection_invalidLndConnectString), 8000);
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                ZapLog.debug(LOG_TAG, "URI could not be parsed");
                showError(getResources().getString(R.string.error_connection_invalidLndConnectString), 8000);
            } catch (NullPointerException e) {
                ZapLog.debug(LOG_TAG, "ConnectURI was null");
                showError(getResources().getString(R.string.error_connection_invalidLndConnectString), 8000);
            }
        } else {
            showError(getResources().getString(R.string.error_connection_unsupported_format), 7000);
        }

    }

    private void connect(String host, int port, String cert, String macaroon) {
        App ctx = App.getAppContext();
        SharedPreferences prefsRemote = Armadillo.create(ctx, PrefsUtil.PREFS_REMOTE)
                .encryptionFingerprint(ctx)
                .keyStretchingFunction(new PBKDF2KeyStretcher(RefConstants.NUM_HASH_ITERATIONS, null))
                .password(ctx.inMemoryPin.toCharArray())
                .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                .build();

        // Do not ask for pin again...
        TimeOutUtil.getInstance().restartTimer();

        // We use commit here, as we want to be sure, that the data is saved and readable when we want to access it in the next step.
        prefsRemote.edit()
                // The following string contains host,port,cert and macaroon in one string separated with ";"
                // This way we can read all necessary data in one call and do not have to execute the key stretching function 4 times.
                .putString(PrefsUtil.REMOTE_COMBINED, host + ";" + port + ";" + cert + ";" + macaroon)
                .commit();

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
