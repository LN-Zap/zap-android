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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.Size;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.util.PermissionsUtil;
import zapsolutions.zap.util.ZapLog;

public abstract class BaseScannerActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = BaseScannerActivity.class.getName();
    protected int mHighlightColor;
    protected int mWhiteColor;
    protected ImageView mScannerInstructionsHelp;
    private ImageButton mBtnFlashlight;
    private TextView mTvPermissionRequired;
    private Button mButtonPaste;
    private Button mButtonHelp;
    private View mQrBorder;

    protected DecoratedBarcodeView mQRCodeScannerView;
    private String mLastText;
    private long mLastTimeStamp;
    private boolean mIsFlashlightActive;


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().isEmpty()) {
                return;
            }

            if (result.getText().equals(mLastText)) {
                if (System.currentTimeMillis() - mLastTimeStamp < 3000) {
                    // Prevent duplicate scans
                    return;
                }
            }

            mLastText = result.getText();
            mLastTimeStamp = System.currentTimeMillis();
            handleCameraResult(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code_scanner);
        setupToolbar();
        mQrBorder = findViewById(R.id.zxing_viewfinder_border);

        mQRCodeScannerView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        mQRCodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        mQRCodeScannerView.decodeContinuous(callback);
        mQRCodeScannerView.setStatusText("");

        mTvPermissionRequired = findViewById(R.id.scannerPermissionRequired);

        // Prepare colors
        mHighlightColor = ContextCompat.getColor(this, R.color.lightningOrange);
        mWhiteColor = ContextCompat.getColor(this, R.color.white);

        mButtonPaste = findViewById(R.id.scannerPaste);
        mButtonPaste.setOnClickListener(this);
        mButtonHelp = findViewById(R.id.scannerHelp);
        mButtonHelp.setOnClickListener(this);
        mBtnFlashlight = findViewById(R.id.scannerFlashButton);
        mBtnFlashlight.setOnClickListener(this);
        mScannerInstructionsHelp = findViewById(R.id.scannerInstructionsHelp);
        mScannerInstructionsHelp.setOnClickListener(this);

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            mBtnFlashlight.setVisibility(View.GONE);
        }

        checkForCameraPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScannerView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mQRCodeScannerView.pause();
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
                    mTvPermissionRequired.setVisibility(View.GONE);
                } else {
                    // Permission denied, show required permission message.
                    mTvPermissionRequired.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void checkForCameraPermission() {
        // Check for camera permission
        if (!PermissionsUtil.hasCameraPermission(this)) {
            PermissionsUtil.requestCameraPermission(this, true);
        }
    }

    public void handleCameraResult(String result) {
        if (result != null) {
            ZapLog.v(LOG_TAG, "Scanned content: " + result);
        }
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
        if (mIsFlashlightActive) { //ToDo
            mIsFlashlightActive = false;
            mQRCodeScannerView.setTorchOff();
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mWhiteColor));
        } else {
            mIsFlashlightActive = true;
            mQRCodeScannerView.setTorchOn();
            mBtnFlashlight.setImageTintList(ColorStateList.valueOf(mHighlightColor));
        }
    }

    protected void showButtonHelp() {
        mButtonHelp.setVisibility(View.VISIBLE);
    }

    protected void setScannerRect(int length) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int rectLength = (int) metrics.scaledDensity * length;
        mQRCodeScannerView.getBarcodeView().setFramingRectSize(new Size(rectLength, rectLength));
        ViewGroup.LayoutParams layoutParams = mQrBorder.getLayoutParams();
        layoutParams.width = (int) metrics.scaledDensity * (length + 5);
        layoutParams.height = (int) metrics.scaledDensity * (length + 5);
        mQrBorder.setLayoutParams(layoutParams);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

}
