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

import androidx.preference.PreferenceManager;
import ln_zap.zap.HomeActivity;
import ln_zap.zap.R;
import ln_zap.zap.qrCodeScanner.BaseScannerActivity;
import ln_zap.zap.util.PermissionsUtil;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ConnectRemoteNodeActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = "Connect to remote node activity";

    private ImageButton mBtnFlashlight;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_connect);
        setupToolbar();

        // Action when clicked on "paste"
        Button btnPaste = findViewById(R.id.scannerPaste);
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
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

    private void connect(){

        // ToDO: Create connection to remote node

        // Mark the wallet es "setup"
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isWalletSetup", true);
        editor.apply();

        // Show home screen, remove history stack
        Intent intent = new Intent(ConnectRemoteNodeActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

        connect();

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
            }
        }
    }

}
