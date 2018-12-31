package ln_zap.zap.qrCodeScanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import ln_zap.zap.R;
import ln_zap.zap.SendActivity;
import ln_zap.zap.util.PermissionsUtil;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QRCodeScannerActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private ImageButton btnFlashlight;
    private int highlightColor;
    private int grayColor;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_scanner);
        setupToolbar();
        mScannerView = new ZBarScannerView(this);

        // Only respond to QR-Codes
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(formats);

        // Prepare colors
        String hexHighlightColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.lightningOrange) & 0x00ffffff);
        highlightColor = Color.parseColor(hexHighlightColor);
        String hexGreyColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.gray) & 0x00ffffff);
        grayColor = Color.parseColor(hexGreyColor);

        // Styling the scanner view
        mScannerView.setLaserEnabled(false);
        mScannerView.setBorderColor(highlightColor);
        mScannerView.setBorderStrokeWidth(20);
        mScannerView.setIsBorderCornerRounded(true);

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
                Intent intent = new Intent(QRCodeScannerActivity.this, SendActivity.class);
                startActivity(intent);
            }
        });

        // Action when clicked on "flash button"
        btnFlashlight = findViewById(R.id.scannerFlashButton);
        btnFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mScannerView.getFlash()){
                    mScannerView.setFlash(false);
                    btnFlashlight.setImageTintList(ColorStateList.valueOf(grayColor));
                }
                else{
                    mScannerView.setFlash(true);
                    btnFlashlight.setImageTintList(ColorStateList.valueOf(highlightColor));
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
        Toast.makeText(this, "Contents = " + rawResult.getContents() +
                ", Format = " + rawResult.getBarcodeFormat().getName(), Toast.LENGTH_SHORT).show();
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
