package zapsolutions.zap.contacts;

import android.content.Intent;
import android.os.Bundle;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.RefConstants;

public class ScanContactActivity extends BaseScannerActivity {

    public static final String EXTRA_NODE_URI = "EXTRA_NODE_URI";
    private static final String LOG_TAG = ScanContactActivity.class.getName();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        try {
            String clipboardContent = ClipBoardUtil.getPrimaryContent(getApplicationContext(), false);
            processUserData(clipboardContent);
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardConnect), RefConstants.ERROR_DURATION_SHORT);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(ScanContactActivity.this, R.string.help_dialog_scanContact);
    }

    @Override
    public void handleCameraResult(String result) {
        super.handleCameraResult(result);

        processUserData(result);
    }

    private boolean processUserData(String rawData) {
        LightningNodeUri nodeUri = LightningParser.parseNodeUri(rawData);

        if (nodeUri == null) {
            showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            return false;
        } else {
            return finishWithNode(nodeUri);
        }
    }

    private boolean finishWithNode(LightningNodeUri nodeUri) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NODE_URI, nodeUri);
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }


}
