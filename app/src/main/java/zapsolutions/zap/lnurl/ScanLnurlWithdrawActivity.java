package zapsolutions.zap.lnurl;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;

import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.NfcUtil;

public class ScanLnurlWithdrawActivity extends BaseScannerActivity {

    private static final String LOG_TAG = ScanLnurlWithdrawActivity.class.getName();

    private NfcAdapter mNfcAdapter;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        //NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        showCameraWithPermissionRequest();
    }


    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        try {
            validateLnurl(ClipBoardUtil.getPrimaryContent(getApplicationContext()));
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardLnurlWithdraw), 4000);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(ScanLnurlWithdrawActivity.this, R.string.help_dialog_scanLnurlWithdraw);
    }

    @Override
    public void handleCameraResult(Result rawResult) {
        super.handleCameraResult(rawResult);

        validateLnurl(rawResult.getContents());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(ScanLnurlWithdrawActivity.this);
            }
        }, 2000);
    }

    private void validateLnurl(String lnurl) {
        // ToDo: implement validation!
    }

    @Override
    public void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, NfcUtil.IntentFilters(), null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NfcUtil.readTag(this, intent, this::validateLnurl);
    }
}
