package zapsolutions.zap.channelManagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;
import zapsolutions.zap.util.ClipBoardUtil;

public class OpenChannelActivity extends BaseScannerActivity {

    public static final String EXTRA_NODE_URI = "EXTRA_NODE_URI";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerInstructions.setText(R.string.scan_qr_code);

        showCameraWithPermissionRequest();
    }

    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        try {
            String clipboardContent = ClipBoardUtil.getPrimaryContent(getApplicationContext());
            processUserData(clipboardContent);
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardConnect), 2000);
        }
    }

    @Override
    public void handleCameraResult(Result rawResult) {
        super.handleCameraResult(rawResult);

        if(!processUserData(rawResult.getContents())) {
            // Note:
            // * Wait 2 seconds to resume the preview.
            // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
            // * I don't know why this is the case but I don't have the time to figure out.
            Handler handler = new Handler();
            handler.postDelayed(() -> mScannerView.resumeCameraPreview(OpenChannelActivity.this), 2000);
        }
    }

    private boolean processUserData(String rawData) {
        LightningNodeUri nodeUri = LightningParser.parseNodeUri(rawData);

        if (nodeUri == null) {
            showError(getResources().getString(R.string.error_lightning_uri_invalid), 5000);
            return false;
        } else {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_NODE_URI, nodeUri);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
    }
}
