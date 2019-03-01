package ln_zap.zap.qrCodeScanner;

import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
    private TextView mTvPermissionRequired;
    private SharedPreferences mPrefs;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_send);
        setupToolbar();

        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);
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
                try {
                    validateInvoice(clipboard.getPrimaryClip().toString());
                } catch (NullPointerException e){
                    showError(getResources().getString(R.string.error_emptyClipboardPayment),4000);
                }
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

            // Our wallet is setup

            // Remove the "lightning:" uri scheme if it is present, LND needs it without uri scheme
            if(invoice.substring(0,10).equalsIgnoreCase("lightning:")){
                invoice = invoice.substring(10);
            }

            // Check if the invoice is a lightning invoice
            if(invoice.substring(0,4).equals("lntb") || invoice.substring(0,4).equals("lnbc")){

                // We have a lightning invoice

                // Check if the invoice is for the same network the app is connected to
                String lnInvoiceType = invoice.substring(0,4);
                if(Wallet.getInstance().isTestnet()){
                    if(lnInvoiceType.equals("lntb")){
                        decodeLightningInvoice(invoice);
                    }
                    else {
                        // Show error. Please use a TESTNET invoice.
                        showError(getResources().getString(R.string.error_useTestnetRequest),5000);
                    }
                }
                else {
                    if(lnInvoiceType.equals("lnbc")){
                        decodeLightningInvoice(invoice);
                    }
                    else {
                        // Show error. Please use a MAINNET invoice.
                        showError(getResources().getString(R.string.error_useMainnetRequest),5000);
                    }
                }

            }
            else{
                // We do not have a lightning invoice... check if it is a valid bitcoin address / invoice

                // ToDo: Do all necessary checks

                // Show error: Not an invoice
                showError(getResources().getString(R.string.error_notAPaymentRequest),7000);
            }


        } else {
            // The wallet is not setup yet, go to next screen to show demo data.
            Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
            intent.putExtra("onChain", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }

    private void decodeLightningInvoice(String invoice){

        // decode lightning invoice
        PayReqString decodePaymentRequest = PayReqString.newBuilder()
                .setPayReq(invoice)
                .build();

        try {
            Wallet.getInstance().mPaymentRequest = LndConnection.getInstance().getBlockingClient().decodePayReq(decodePaymentRequest);
            ZapLog.debug(LOG_TAG, Wallet.getInstance().mPaymentRequest.toString());

            if (Wallet.getInstance().mPaymentRequest.getTimestamp() + Wallet.getInstance().mPaymentRequest.getExpiry() < System.currentTimeMillis() / 1000) {
                // Show error: payment request expired.
                showError(getResources().getString(R.string.error_paymentRequestExpired),3000);
            } else {
                // Decoded successfully, go to send page.
                Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
                intent.putExtra("onChain", false);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);

            }

        } catch (StatusRuntimeException e) {
            // If LND can't decode the payment request, show the error LND throws (always english)
            showError(e.getMessage(),3000);
            Wallet.getInstance().mPaymentRequest = null;
            e.printStackTrace();
        }
    }

    private void showError(String message, int duration){
        Snackbar msg = Snackbar.make(findViewById(android.R.id.content).getRootView(),message,Snackbar.LENGTH_LONG);
        View sbView = msg.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.superRed));
        msg.setDuration(duration);
        msg.show();
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
