package zapsolutions.zap;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.InvoiceUtil;
import zapsolutions.zap.util.NfcUtil;

public class SendActivity extends BaseScannerActivity {

    private static final String LOG_TAG = SendActivity.class.getName();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
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
            validateInvoice(ClipBoardUtil.getPrimaryContent(getApplicationContext()));
        } catch (NullPointerException e) {
            showError(getResources().getString(R.string.error_emptyClipboardPayment), 4000);
        }
    }

    @Override
    public void onButtonInstructionsHelpClick() {
        HelpDialogUtil.showDialog(SendActivity.this, R.string.help_dialog_scanPaymentRequest);
    }

    @Override
    public void handleCameraResult(Result rawResult) {
        super.handleCameraResult(rawResult);

        validateInvoice(rawResult.getContents());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(SendActivity.this);
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void validateInvoice(String invoice) {

        InvoiceUtil.readInvoice(SendActivity.this, compositeDisposable, invoice, new InvoiceUtil.OnReadInvoiceCompletedListener() {
            @Override
            public void onValidLightningInvoice(PayReq paymentRequest, String invoice) {
                goToLightningPaymentScreen(paymentRequest, invoice);
            }

            @Override
            public void onValidBitcoinInvoice(String address, long amount, String message) {
                goToOnChainPaymentScreen(address, amount, message);
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }
        });

    }

    private void goToOnChainPaymentScreen(String address, long amount, String message) {
        Intent intent = new Intent();
        intent.putExtra("onChain", true);
        intent.putExtra("onChainAddress", address);
        intent.putExtra("onChainAmount", amount);
        intent.putExtra("onChainMessage", message);
        setResult(1, intent);
        finish();
    }


    private void goToLightningPaymentScreen(PayReq paymentRequest, String invoice) {
        Intent intent = new Intent();
        intent.putExtra("onChain", false);
        intent.putExtra("lnPaymentRequest", paymentRequest.toByteArray());
        intent.putExtra("lnInvoice", invoice);
        setResult(1, intent);
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
        NfcUtil.readTag(this, intent, this::validateInvoice);
    }
}
