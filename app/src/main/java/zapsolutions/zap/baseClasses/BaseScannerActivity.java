package zapsolutions.zap.baseClasses;


import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import zapsolutions.zap.R;

import java.util.ArrayList;

public class BaseScannerActivity extends BaseAppCompatActivity {
    protected ZBarScannerView mScannerView;
    protected int mHighlightColor;
    protected int mGrayColor;

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerView = new ZBarScannerView(this);

        // Only respond to QR-Codes
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(formats);

        // Prepare colors
        mHighlightColor = ContextCompat.getColor(this, R.color.lightningOrange);
        mGrayColor = ContextCompat.getColor(this, R.color.gray);

        // Styling the scanner view
        mScannerView.setLaserEnabled(false);
        mScannerView.setBorderColor(mHighlightColor);
        mScannerView.setBorderStrokeWidth(20);
        mScannerView.setIsBorderCornerRounded(true);

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
}
