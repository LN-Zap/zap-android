package zapsolutions.zap.lnurl;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.LnurlDecoder;
import zapsolutions.zap.util.NfcUtil;
import zapsolutions.zap.util.ZapLog;

public class ScanLnUrlWithdrawActivity extends BaseScannerActivity {

    private static final String LOG_TAG = ScanLnUrlWithdrawActivity.class.getName();

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
            validateLnUrl(ClipBoardUtil.getPrimaryContent(getApplicationContext()));
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardLnurlWithdraw), 4000);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(ScanLnUrlWithdrawActivity.this, R.string.help_dialog_scanLnurlWithdraw);
    }

    @Override
    public void handleCameraResult(Result rawResult) {
        super.handleCameraResult(rawResult);

        validateLnUrl(rawResult.getContents());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(ScanLnUrlWithdrawActivity.this);
            }
        }, 2000);
    }

    private void validateLnUrl(String lnUrl) {
        try {
            String decodedLnUrl = LnurlDecoder.decode(lnUrl);

            StringRequest lnurlRequest = new StringRequest(Request.Method.GET, decodedLnUrl,
                    response -> validateFirstResponse(response),
                    error -> {
                        URL url = null;
                        try {
                            url = new URL(decodedLnUrl);
                            String host = url.getHost();
                            showError(getResources().getString(R.string.lnurl_service_not_responding, host), 4000);
                        } catch (MalformedURLException e) {
                            String host = getResources().getString(R.string.host);
                            showError(getResources().getString(R.string.lnurl_service_not_responding, host), 4000);
                            e.printStackTrace();
                        }
                    });

            ZapLog.debug(LOG_TAG, "LNURL: Requesting withdraw data...");
            HttpClient.getInstance().addToRequestQueue(lnurlRequest, "LnUrlWithdrawRequest");

        } catch (Exception e) {
            ZapLog.debug(LOG_TAG, e.getMessage());
            showError(getResources().getString(R.string.lnurl_decoding_failed), 4000);
        }
    }

    private void validateFirstResponse(@NonNull String withdrawResponse) {
        LnUrlWithdrawResponse lnUrlWithdrawResponse = new Gson().fromJson(withdrawResponse, LnUrlWithdrawResponse.class);

        if (lnUrlWithdrawResponse.hasError()) {
            showError(lnUrlWithdrawResponse.getReason(), 4000);
        } else {
            if (lnUrlWithdrawResponse.isWithdraw()) {
                goToLnurlWithdrawScreen(lnUrlWithdrawResponse);
            } else {
                showError(getResources().getString(R.string.lnurl_wrong_tag), 4000);
            }
        }
    }

    private void goToLnurlWithdrawScreen(LnUrlWithdrawResponse lnurlWithdrawResponse) {
        ZapLog.debug(LOG_TAG, "LNURL: valid data received...");

        Bundle bundle = new Bundle();
        bundle.putSerializable(LnUrlWithdrawResponse.ARGS_KEY, lnurlWithdrawResponse);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(HomeActivity.RESULT_CODE_LNURL_WITHDRAW, intent);
        finish();
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
        NfcUtil.readTag(this, intent, this::validateLnUrl);
    }
}
