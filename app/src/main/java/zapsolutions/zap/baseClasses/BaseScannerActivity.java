package zapsolutions.zap.baseClasses;


import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
    protected int mGrayColor;
    protected TextView mScannerInstructions;
    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;
    private Button mButtonPaste;
    private Button mButtonHelp;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_scanner);
        setupToolbar();

        mScannerView = new ZBarScannerView(this);
        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);
        mScannerInstructions = findViewById(R.id.scannerInstructions);

        // Only respond to QR-Codes
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(formats);

        // Prepare colors
        mHighlightColor = ContextCompat.getColor(this, R.color.lightningOrange);
        mGrayColor = ContextCompat.getColor(this, R.color.gray);

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
            ZapLog.debug(LOG_TAG, result.getContents());
        }
    }

    public void onButtonPasteClick() {
        // handled in subclass
    }

    public void onButtonHelpClick() {
        // handled in subclass
    }

    public void onButtonFlashClick() {
        if (mScannerView.getFlash()) {
            mScannerView.setFlash(false);
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mGrayColor));
        } else {
            mScannerView.setFlash(true);
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mHighlightColor));
        }
    }

    protected void showButtonHelp() {
        mButtonHelp.setVisibility(View.VISIBLE);
    }

}
