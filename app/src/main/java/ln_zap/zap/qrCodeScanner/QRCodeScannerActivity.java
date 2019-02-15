package ln_zap.zap.qrCodeScanner;

import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lightningnetwork.lnd.lnrpc.PayReqString;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import io.grpc.StatusRuntimeException;
import ln_zap.zap.R;
import ln_zap.zap.SendActivity;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.util.PermissionsUtil;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QRCodeScannerActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = "QR-Code Activity";

    private ImageButton mBtnFlashlight;
    private SharedPreferences mPrefs;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_send);
        setupToolbar();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(QRCodeScannerActivity.this);

        // Check for camera permission
        if (PermissionsUtil.hasCameraPermission(QRCodeScannerActivity.this)){
            showCameraView();
        }
        else{
            PermissionsUtil.requestCameraPermission(QRCodeScannerActivity.this,true);
        }

    }

    private void showCameraView(){
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(mScannerView);

        // Action when clicked on "paste"
        Button btnPaste = findViewById(R.id.scannerPaste);
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                validateInvoice(clipboard.getPrimaryClip().toString());
            }
        });

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

        validateInvoice(rawResult.getContents());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(QRCodeScannerActivity.this);
            }
        }, 2000);
    }

    private void validateInvoice(String invoice){

        if (mPrefs.getBoolean("isWalletSetup", false)) {
            // Check if it is a testnet lightning invoice. These checks have to be made more seriously later.
            if (invoice.contains("lntb")) {
                String input = invoice.substring(10);
                ZapLog.debug(LOG_TAG, input);

                // decode lightning invoice
                PayReqString decodePaymentRequest = PayReqString.newBuilder()
                        .setPayReq(input)
                        .build();

                try {
                    Wallet.getInstance().mPaymentRequest = LndConnection.getInstance().getBlockingClient().decodePayReq(decodePaymentRequest);
                    ZapLog.debug(LOG_TAG, Wallet.getInstance().mPaymentRequest.toString());

                    if (Wallet.getInstance().mPaymentRequest.getTimestamp() + Wallet.getInstance().mPaymentRequest.getExpiry() < System.currentTimeMillis() / 1000) {
                        Toast.makeText(this, "payment request expired", Toast.LENGTH_SHORT).show();
                    } else {

                        Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
                        intent.putExtra("onChain", false);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);

                    }

                } catch (StatusRuntimeException e) {
                    Toast.makeText(this, "unable to decode payment request", Toast.LENGTH_SHORT).show();
                    Wallet.getInstance().mPaymentRequest = null;
                    e.printStackTrace();
                }


            } else {
                Snackbar msg = Snackbar.make(findViewById(android.R.id.content).getRootView(),R.string.error_invalidPaymentReqest,Snackbar.LENGTH_LONG);
                View sbView = msg.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.superRed));
                msg.setDuration(7000);
                msg.show();
            }
        } else {
            // The wallet is not setup yet, go to next screen without data.
            Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
            intent.putExtra("onChain", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
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
                    // Permission denied, go to send activity immediately.
                    Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

}
