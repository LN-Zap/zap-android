package ln_zap.zap.setup;

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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Ref;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import at.favre.lib.armadillo.Armadillo;
import ln_zap.zap.HomeActivity;
import ln_zap.zap.R;
import ln_zap.zap.RefConstants;
import ln_zap.zap.baseClasses.App;
import ln_zap.zap.qrCodeScanner.BaseScannerActivity;
import ln_zap.zap.util.PermissionsUtil;
import ln_zap.zap.util.TimeOutUtil;
import ln_zap.zap.util.ZapLog;
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
                try {
                    verifyDesiredConnection(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
                } catch (NullPointerException e) {
                    showError(getResources().getString(R.string.error_emptyClipboardConnect), 4000);
                }
            }
        });

        // ToDO: This is just for development and has to be removed later!!!
        btnPaste.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                verifyDesiredConnection("lndconnect://157.230.97.66:10009?cert=MIIB6zCCAZGgAwIBAgIRALk4MnZPN5DY9Zblrfdg26swCgYIKoZIzj0EAwIwMTEfMB0GA1UEChMWbG5kIGF1dG9nZW5lcmF0ZWQgY2VydDEOMAwGA1UEAxMFWmFwMDEwHhcNMTkwMTAzMTEzNjUxWhcNMjAwMjI4MTEzNjUxWjAxMR8wHQYDVQQKExZsbmQgYXV0b2dlbmVyYXRlZCBjZXJ0MQ4wDAYDVQQDEwVaYXAwMTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABIM_5RHSfEsj3bDD56goP8Fm4u8Kdh7DPyRZ8-xVEAz7YwXLm2zLJLYxCSBeMPbRKs7F5CuRePKrgBFr6tDdPoGjgYkwgYYwDgYDVR0PAQH_BAQDAgKkMA8GA1UdEwEB_wQFMAMBAf8wYwYDVR0RBFwwWoIFWmFwMDGCCWxvY2FsaG9zdIIEdW5peIIKdW5peHBhY2tldIcEfwAAAYcQAAAAAAAAAAAAAAAAAAAAAYcEneZhQocEChMABYcQ_oAAAAAAAABsUEr__iJjMjAKBggqhkjOPQQDAgNIADBFAiBWsPEhXswlcj2aVd05v6wjf5jBe_OCyjZEu5PRbMSzuQIhAJpZXCg62zy6jt6S0LenZ7o-X3yiByRpoeFbVjfo5jQo&macaroon=AgEDbG5kAs8BAwoQ7a5y27C7Q4_FQFYaHP2uuBIBMBoWCgdhZGRyZXNzEgRyZWFkEgV3cml0ZRoTCgRpbmZvEgRyZWFkEgV3cml0ZRoXCghpbnZvaWNlcxIEcmVhZBIFd3JpdGUaFgoHbWVzc2FnZRIEcmVhZBIFd3JpdGUaFwoIb2ZmY2hhaW4SBHJlYWQSBXdyaXRlGhYKB29uY2hhaW4SBHJlYWQSBXdyaXRlGhQKBXBlZXJzEgRyZWFkEgV3cml0ZRoSCgZzaWduZXISCGdlbmVyYXRlAAAGIKUDrvb9TjXUpc3Dca_8zSZ6wcI4PWg7mqaPxh_oZZAX");
                return false;
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

        URI connectURI = null;
        try {
            connectURI = new URI(connectString);
            if (!connectURI.getScheme().equals("lndconnect")) {
                showError(getResources().getString(R.string.error_invalidRemoteConnectionString), 4000);
            } else {

                String cert = null;
                String macaroon = null;

                // Fetch params
                if (connectURI.getQuery() != null) {
                    String[] valuePairs = connectURI.getQuery().split("&");
                    for (String pair : valuePairs) {
                        String[] param = pair.split("=");
                        if (param[0].equals("cert")) {
                            cert = param[1];
                        }
                        if (param[0].equals("macaroon")) {
                            macaroon = param[1];
                        }
                    }

                    // Everything is ok, initiate connection
                    connect(connectURI.getHost(), connectURI.getPort(), cert, macaroon);

                } else {
                    ZapLog.debug(LOG_TAG, "Connect URI has not parameters");
                    showError(getResources().getString(R.string.error_invalidRemoteConnectionString), 4000);
                }
            }
        } catch (URISyntaxException e) {
            ZapLog.debug(LOG_TAG, "URI could not be parsed");
            e.printStackTrace();
            showError(getResources().getString(R.string.error_invalidRemoteConnectionString), 4000);
        }

    }

    private void connect(String host, int port, String cert, String macaroon) {
        App ctx = App.getAppContext();
        SharedPreferences prefsRemote = Armadillo.create(ctx, RefConstants.prefs_remote)
                .encryptionFingerprint(ctx)
                .password(ctx.inMemoryPin.toCharArray())
                .build();

        prefsRemote.edit()
                .putString(RefConstants.remote_host, host)
                .putInt(RefConstants.remote_port, port)
                .putString(RefConstants.remote_cert, cert)
                .putString(RefConstants.remote_macaroon, macaroon)
                .apply();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putBoolean("isWalletSetup", true)
                .apply();

        // Do not ask for pin again...
        TimeOutUtil.getInstance().startTimer();

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

        verifyDesiredConnection(rawResult.getContents());

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
