package zapsolutions.zap.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;

import java.net.URL;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.lnurl.channel.LnUrlHostedChannelResponse;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.lnurl.withdraw.LnUrlWithdrawResponse;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ClipBoardUtil {
    private static final String LOG_TAG = ClipBoardUtil.class.getName();

    public static void copyToClipboard(Context context, String label, CharSequence data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, data);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPrimaryContent(Context context) throws NullPointerException {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        return clipboard.getPrimaryClip().getItemAt(0).getText().toString();
    }


    public static void performClipboardScan(Context context, CompositeDisposable compositeDisposable, OnClipboardScanProceedListener listener) {

        if (!PrefsUtil.getPrefs().getBoolean(PrefsUtil.SCAN_CLIPBOARD, true)) {
            return;
        }

        try {
            getPrimaryContent(context);
        } catch (NullPointerException e) {
            return;
        }

        String clipboardContent = getPrimaryContent(context);
        String clipboardContentHash = UtilFunctions.sha256Hash(clipboardContent);

        if (PrefsUtil.getPrefs().getString(PrefsUtil.LAST_CLIPBOARD_SCAN, "").equals(clipboardContentHash)) {
            ZapLog.v(LOG_TAG, "Clipboard with same content was checked before");
            return;
        } else {
            PrefsUtil.edit().putString(PrefsUtil.LAST_CLIPBOARD_SCAN, clipboardContentHash).apply();
        }

        ZapLog.v(LOG_TAG, "New Clipboard content found!");

        /* We are not allowed to access LNURL links twice.
        Therefore we first have to check if it is a LNURL and then hand over to the listener to perform the action.
        Executing the rest twice doesn't harm anyone.
         */
        try {
            URL url = new URL(clipboardContent);
            String query = url.getQuery();
            if (query != null && query.contains("lightning=LNURL1")) {
                showProceedQuestion(R.string.clipboard_scan_lnurl, context, listener);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            LnurlDecoder.decode(clipboardContent);
            showProceedQuestion(R.string.clipboard_scan_lnurl, context, listener);
            return;
        } catch (Exception ignored) {
        }

        BitcoinStringAnalyzer.analyze(context, compositeDisposable, clipboardContent, new BitcoinStringAnalyzer.OnDataDecodedListener() {
            @Override
            public void onValidLightningInvoice(PayReq paymentRequest, String invoice) {
                showProceedQuestion(R.string.clipboard_scan_payment, context, listener);
            }

            @Override
            public void onValidBitcoinInvoice(String address, long amount, String message) {
                showProceedQuestion(R.string.clipboard_scan_payment, context, listener);
            }

            @Override
            public void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse) {
                // never reached
            }

            @Override
            public void onValidLnUrlChannel(LnUrlChannelResponse channelResponse) {
                // never reached
            }

            @Override
            public void onValidLnUrlHostedChannel(LnUrlHostedChannelResponse hostedChannelResponse) {
                // never reached
            }

            @Override
            public void onValidLnUrlPay(LnUrlPayResponse payResponse) {
                // never reached
            }

            @Override
            public void onValidLnUrlAuth(URL url) {
                // never reached
            }

            @Override
            public void onValidLndConnectString(RemoteConfiguration remoteConfiguration) {
                showProceedQuestion(R.string.clipboard_scan_connect, context, listener);
            }

            @Override
            public void onValidBTCPayConnectData(RemoteConfiguration remoteConfiguration) {
                showProceedQuestion(R.string.clipboard_scan_connect, context, listener);
            }

            @Override
            public void onValidNodeUri(LightningNodeUri nodeUri) {
                showProceedQuestion(R.string.clipboard_scan_node_pubkey, context, listener);
            }

            @Override
            public void onError(String error, int duration) {
                String errorMessage = context.getString(R.string.clipboard_error_heading) + "\n\n" + error;
                listener.onError(errorMessage, duration + 1000);
            }

            @Override
            public void onNoReadableData() {
                // in case we have unrecognized data, we just want to ignore it
            }
        });
    }

    private static void showProceedQuestion(int question, Context context, OnClipboardScanProceedListener listener) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context)
                .setMessage(question)
                .setCancelable(true)
                .setPositiveButton(R.string.continue_string, (dialog, whichButton) -> listener.onProceed(getPrimaryContent(context)))
                .setNegativeButton(R.string.no, (dialog, which) -> {
                });

        Dialog dlg = adb.create();
        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        dlg.show();
    }

    public interface OnClipboardScanProceedListener {
        void onProceed(String content);

        void onError(String error, int duration);
    }
}
