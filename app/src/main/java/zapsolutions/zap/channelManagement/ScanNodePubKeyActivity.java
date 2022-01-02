package zapsolutions.zap.channelManagement;

import android.content.Intent;
import android.os.Bundle;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;

import java.net.URL;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.lnurl.channel.LnUrlHostedChannelResponse;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.lnurl.withdraw.LnUrlWithdrawResponse;
import zapsolutions.zap.util.BitcoinStringAnalyzer;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.RefConstants;

public class ScanNodePubKeyActivity extends BaseScannerActivity implements LightningNodeRecyclerAdapter.LightningNodeSelectedListener {

    public static final int RESULT_CODE_NODE_URI = 1;
    public static final int RESULT_CODE_LNURL_CHANNEL = 2;
    public static final String EXTRA_NODE_URI = "EXTRA_NODE_URI";
    public static final String EXTRA_CHANNEL_RESPONSE = "EXTRA_CHANNEL_RESPONSE";
    private static final String LOG_TAG = ScanNodePubKeyActivity.class.getName();

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onButtonPasteClick() {
        super.onButtonPasteClick();

        try {
            String clipboardContent = ClipBoardUtil.getPrimaryContent(getApplicationContext(), true);
            processUserData(clipboardContent);
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardConnect), RefConstants.ERROR_DURATION_SHORT);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(ScanNodePubKeyActivity.this, R.string.help_dialog_scanNodePublicKey);
    }

    @Override
    public void handleCameraResult(String result) {
        super.handleCameraResult(result);

        processUserData(result);
    }

    private void processUserData(String rawData) {
        BitcoinStringAnalyzer.analyze(ScanNodePubKeyActivity.this, mCompositeDisposable, rawData, new BitcoinStringAnalyzer.OnDataDecodedListener() {
            @Override
            public void onValidLightningInvoice(PayReq paymentRequest, String invoice) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidBitcoinInvoice(String address, long amount, String message, String lightningInvoice) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidLnUrlChannel(LnUrlChannelResponse channelResponse) {
                finishWithLnUrlChannel(channelResponse);
            }

            @Override
            public void onValidLnUrlHostedChannel(LnUrlHostedChannelResponse hostedChannelResponse) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidLnUrlPay(LnUrlPayResponse payResponse) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidLnUrlAuth(URL url) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidInternetIdentifier(LnUrlPayResponse payResponse) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidLndConnectString(RemoteConfiguration remoteConfiguration) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidBTCPayConnectData(RemoteConfiguration remoteConfiguration) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onValidNodeUri(LightningNodeUri nodeUri) {
                finishWithNode(nodeUri);
            }

            @Override
            public void onValidURL(String url) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onError(String error, int duration) {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }

            @Override
            public void onNoReadableData() {
                showError(getResources().getString(R.string.error_lightning_uri_invalid), RefConstants.ERROR_DURATION_LONG);
            }
        });

    }

    private boolean finishWithNode(LightningNodeUri nodeUri) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NODE_URI, nodeUri);
        setResult(RESULT_CODE_NODE_URI, intent);
        finish();
        return true;
    }

    private boolean finishWithLnUrlChannel(LnUrlChannelResponse channelResponse) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CHANNEL_RESPONSE, channelResponse);
        setResult(RESULT_CODE_LNURL_CHANNEL, intent);
        finish();
        return true;
    }

    @Override
    public void onNodeSelected(LightningNodeUri suggestedLightningNode) {
        finishWithNode(suggestedLightningNode);
    }
}
