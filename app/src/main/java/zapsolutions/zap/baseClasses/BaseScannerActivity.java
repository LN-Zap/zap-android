package zapsolutions.zap.baseClasses;


import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import zapsolutions.zap.R;
import zapsolutions.zap.util.PermissionsUtil;
import zapsolutions.zap.util.ZapLog;

public abstract class BaseScannerActivity extends BaseAppCompatActivity implements ZBarScannerView.ResultHandler, View.OnClickListener {
    private static final String LOG_TAG = BaseScannerActivity.class.getName();
    protected ZBarScannerView mScannerView;
    protected int mHighlightColor;
    protected int mWhiteColor;
    protected ImageView mScannerInstructionsHelp;
    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;
    private Button mButtonPaste;
    private Button mButtonHelp;
    private Handler mHandler;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mHandler = new Handler();
        setContentView(R.layout.activity_qr_code_scanner);
        setupToolbar();

        mScannerView = new ZBarScannerView(this);
        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);

        // Only respond to QR-Codes
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(formats);

        // Prepare colors
        mHighlightColor = ContextCompat.getColor(this, R.color.lightningOrange);
        mWhiteColor = ContextCompat.getColor(this, R.color.white);

        // Scanner settings
        mScannerView.setAspectTolerance(0.5f);

        // Styling the scanner view
        mScannerView.setSquareViewFinder(true);
        mScannerView.setLaserEnabled(false);
        mScannerView.setBorderColor(mHighlightColor);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int strokeWidth = metrics.densityDpi / 25;
        mScannerView.setBorderStrokeWidth(strokeWidth);
        mScannerView.setIsBorderCornerRounded(true);

        mButtonPaste = findViewById(R.id.scannerPaste);
        mButtonPaste.setOnClickListener(this);

        mButtonHelp = findViewById(R.id.scannerHelp);
        mButtonHelp.setOnClickListener(this);

        mBtnFlashlight = findViewById(R.id.scannerFlashButton);
        mBtnFlashlight.setOnClickListener(this);

        mScannerInstructionsHelp = findViewById(R.id.scannerInstructionsHelp);
        mScannerInstructionsHelp.setOnClickListener(this);
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
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scannerPaste:
                onButtonPasteClick();
                break;
            case R.id.scannerHelp:
                onButtonHelpClick();
                break;
            case R.id.scannerFlashButton:
                onButtonFlashClick();
                break;
            case R.id.scannerInstructionsHelp:
                onButtonInstructionsHelpClick();
                break;
        }
    }

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

    @Override
    public void handleResult(Result result) {
        handleCameraResult(result);
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void showCameraView() {
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(mScannerView);
    }

    public void showCameraWithPermissionRequest() {
        // Check for camera permission
        if (PermissionsUtil.hasCameraPermission(this)) {
            showCameraView();
        } else {
            PermissionsUtil.requestCameraPermission(this, true);
        }
    }

    public void handleCameraResult(Result result) {
        if (result != null) {
            ZapLog.debug(LOG_TAG, "Scanned content: " + result.getContents());
        }

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(BaseScannerActivity.this);
            }
        }, 2000);
    }

    public void onButtonPasteClick() {
        // handled in subclass
    }

    public void onButtonHelpClick() {
        // handled in subclass
    }

    public void onButtonInstructionsHelpClick() {
        // handled in subclass
    }

    public void onButtonFlashClick() {
        if (mScannerView.getFlash()) {
            mScannerView.setFlash(false);
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mWhiteColor));
        } else {
            mScannerView.setFlash(true);
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mHighlightColor));
        }
    }

    protected void showButtonHelp() {
        mButtonHelp.setVisibility(View.VISIBLE);
    }

}
