package zapsolutions.zap.lnurl.withdraw;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import java.net.URL;

import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.lnurl.channel.LnUrlHostedChannelResponse;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.LnUrlUtil;
import zapsolutions.zap.util.NfcUtil;
import zapsolutions.zap.util.RefConstants;

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
            showError(getResources().getString(R.string.error_emptyClipboardLnurlWithdraw), RefConstants.ERROR_DURATION_SHORT);
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
    }

    private void validateLnUrl(String lnUrl) {
        LnUrlUtil.readLnUrl(this, lnUrl, new LnUrlUtil.OnLnUrlReadListener() {
            @Override
            public void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse) {
                goToLnurlWithdrawScreen(withdrawResponse);
            }

            @Override
            public void onValidLnUrlPay(LnUrlPayResponse payResponse) {
                showError(getResources().getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_SHORT);
            }

            @Override
            public void onValidLnUrlChannel(LnUrlChannelResponse channelResponse) {
                showError(getResources().getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_SHORT);
            }

            @Override
            public void onValidLnUrlHostedChannel(LnUrlHostedChannelResponse hostedChannelResponse) {
                showError(getResources().getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_SHORT);
            }

            @Override
            public void onValidLnUrlAuth(URL url) {
                showError(getResources().getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_SHORT);
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }

            @Override
            public void onNoLnUrlData() {
                showError(getResources().getString(R.string.lnurl_decoding_no_lnurl_data), RefConstants.ERROR_DURATION_SHORT);
            }
        });
    }

    private void goToLnurlWithdrawScreen(LnUrlWithdrawResponse lnurlWithdrawResponse) {
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
