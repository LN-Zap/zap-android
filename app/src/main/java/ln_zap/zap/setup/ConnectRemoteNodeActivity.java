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

import java.net.URI;
import java.net.URISyntaxException;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import ln_zap.zap.HomeActivity;
import ln_zap.zap.R;
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
                verifyDesiredConnection(clipboard.getPrimaryClip().toString());
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
        if (PermissionsUtil.hasCameraPermission(ConnectRemoteNodeActivity.this)){
            showCameraView();
        }
        else{
            PermissionsUtil.requestCameraPermission(ConnectRemoteNodeActivity.this,true);
        }
    }

    private void showCameraView(){
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(mScannerView);

        // Action when clicked on "flash button"
        mBtnFlashlight = findViewById(R.id.scannerFlashButton);
        mBtnFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mScannerView.getFlash()){
                    mScannerView.setFlash(false);
                    mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mGrayColor));
                }
                else{
                    mScannerView.setFlash(true);
                    mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mHighlightColor));
                }
            }
        });
    }

    private void verifyDesiredConnection(String connectString){

        // Uncomment this to connect to dev node
        //connectString = "lndconnect://157.230.97.66:10009?cert=MIIB6zCCAZGgAwIBAgIRALk4MnZPN5DY9Zblrfdg26swCgYIKoZIzj0EAwIwMTEfMB0GA1UEChMWbG5kIGF1dG9nZW5lcmF0ZWQgY2VydDEOMAwGA1UEAxMFWmFwMDEwHhcNMTkwMTAzMTEzNjUxWhcNMjAwMjI4MTEzNjUxWjAxMR8wHQYDVQQKExZsbmQgYXV0b2dlbmVyYXRlZCBjZXJ0MQ4wDAYDVQQDEwVaYXAwMTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABIM_5RHSfEsj3bDD56goP8Fm4u8Kdh7DPyRZ8-xVEAz7YwXLm2zLJLYxCSBeMPbRKs7F5CuRePKrgBFr6tDdPoGjgYkwgYYwDgYDVR0PAQH_BAQDAgKkMA8GA1UdEwEB_wQFMAMBAf8wYwYDVR0RBFwwWoIFWmFwMDGCCWxvY2FsaG9zdIIEdW5peIIKdW5peHBhY2tldIcEfwAAAYcQAAAAAAAAAAAAAAAAAAAAAYcEneZhQocEChMABYcQ_oAAAAAAAABsUEr__iJjMjAKBggqhkjOPQQDAgNIADBFAiBWsPEhXswlcj2aVd05v6wjf5jBe_OCyjZEu5PRbMSzuQIhAJpZXCg62zy6jt6S0LenZ7o-X3yiByRpoeFbVjfo5jQo&macaroon=AgEDbG5kAs8BAwoQ7a5y27C7Q4_FQFYaHP2uuBIBMBoWCgdhZGRyZXNzEgRyZWFkEgV3cml0ZRoTCgRpbmZvEgRyZWFkEgV3cml0ZRoXCghpbnZvaWNlcxIEcmVhZBIFd3JpdGUaFgoHbWVzc2FnZRIEcmVhZBIFd3JpdGUaFwoIb2ZmY2hhaW4SBHJlYWQSBXdyaXRlGhYKB29uY2hhaW4SBHJlYWQSBXdyaXRlGhQKBXBlZXJzEgRyZWFkEgV3cml0ZRoSCgZzaWduZXISCGdlbmVyYXRlAAAGIKUDrvb9TjXUpc3Dca_8zSZ6wcI4PWg7mqaPxh_oZZAX";


        URI connectURI = null;
            try {
                connectURI = new URI(connectString);
                if (!connectURI.getScheme().equals("lndconnect")) {
                    connectionError();
                } else {

                    String cert = null;
                    String macaroon = null;

                    // Fetch params
                    String[] valuePairs = connectURI.getQuery().split("&");
                    for (String pair : valuePairs) {
                        String[] param = pair.split("=");
                        if (param[0].equals("cert")){
                            cert = param[1];
                        }
                        if (param[0].equals("macaroon")){
                            macaroon = param[1];
                        }
                    }

                    // Everything is ok, initiate connection
                    connect(connectURI.getHost(), connectURI.getPort(), cert, macaroon);
                }
            }
            catch (URISyntaxException e){
                ZapLog.debug(LOG_TAG, "URI could not be parsed");
                e.printStackTrace();
                connectionError();
            }

    }

    private void connect(String host, int port, String cert, String macaroon){

        // Save connection as plain text in preferences (UNSECURE!)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("remoteHost", host);
        editor.putInt("remotePort", port);
        editor.putString("remoteCert", cert);
        editor.putString("remoteMacaroon", macaroon);
        editor.putBoolean("isWalletSetup", true);
        editor.apply();

        // Do not ask for pin again...
        TimeOutUtil.getInstance().startTimer();

        // Show home screen, remove history stack
        Intent intent = new Intent(ConnectRemoteNodeActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void connectionError(){
        Snackbar msg = Snackbar.make(findViewById(android.R.id.content).getRootView(),R.string.error_invalidRemoteConnectionString,Snackbar.LENGTH_LONG);
        View sbView = msg.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.superRed));
        msg.setDuration(4000);
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
                }
                else {
                    // Permission denied, show required permission message.
                    mTvPermissionRequired.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
