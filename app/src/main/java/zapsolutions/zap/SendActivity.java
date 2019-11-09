package zapsolutions.zap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.PayReqString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import me.dm7.barcodescanner.zbar.Result;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseScannerActivity;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.InvoiceUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class SendActivity extends BaseScannerActivity {

    private static final String LOG_TAG = SendActivity.class.getName();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean mFromURIScheme = false;
    private String mOnChainAddress;
    private long mOnChainInvoiceAmount;
    private String mOnChainInvoiceMessage;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerInstructions.setText(R.string.send_scan_info);

        if (App.getAppContext().getUriSchemeData() != null) {

            // This activity was invoked because the app was started from an url (lightning: or bitcoin:)
            // The url will be validated and the activity is finished before it will actually be shown.
            String invoice = App.getAppContext().getUriSchemeData();
            App.getAppContext().setUriSchemeData(null);
            mFromURIScheme = true;
            validateInvoice(invoice);
        } else {
            showCameraWithPermissionRequest();
        }
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
    protected void showError(String message, int duration) {
        if (mFromURIScheme) {
            Intent intent = new Intent();
            intent.putExtra("error", message);
            intent.putExtra("error_duration", duration);
            setResult(1, intent);
            finish();
        } else {
            super.showError(message, duration);
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void validateInvoice(String invoice) {

        mOnChainAddress = null;
        mOnChainInvoiceAmount = 0L;
        mOnChainInvoiceMessage = null;

        if (PrefsUtil.isWalletSetup()) {

            // Our wallet is setup

            // Avoid index out of bounds. An invoice with less than 11 characters isn't valid.
            if (invoice.length() < 11) {
                showError(getResources().getString(R.string.error_notAPaymentRequest), 7000);
                return;
            }

            // convert to lower case
            String lnInvoice = invoice.toLowerCase();

            // Remove the "lightning:" uri scheme if it is present, LND needs it without uri scheme
            if (InvoiceUtil.isLightningUri(lnInvoice)) {
                lnInvoice = lnInvoice.substring(InvoiceUtil.URI_PREFIX_LIGHTNING.length());
            }

            // Check if the invoice is a lightning invoice
            if (InvoiceUtil.isLightningInvoice(lnInvoice)) {

                // We have a lightning invoice

                // Check if the invoice is for the same network the app is connected to
                String lnInvoiceType = lnInvoice.substring(0, 4);
                if (Wallet.getInstance().isTestnet()) {
                    if (lnInvoiceType.equals(InvoiceUtil.INVOICE_PREFIX_LIGHTNING_TESTNET)) {
                        decodeLightningInvoice(lnInvoice);
                    } else {
                        // Show error. Please use a TESTNET invoice.
                        showError(getResources().getString(R.string.error_useTestnetRequest), 5000);
                    }
                } else {
                    if (lnInvoiceType.equals(InvoiceUtil.INVOICE_PREFIX_LIGHTNING_MAINNET)) {
                        decodeLightningInvoice(lnInvoice);
                    } else {
                        // Show error. Please use a MAINNET invoice.
                        showError(getResources().getString(R.string.error_useMainnetRequest), 5000);
                    }
                }

            } else {
                // We do not have a lightning invoice... check if it is a valid bitcoin address / invoice

                // Check if we have a bitcoin invoice with the "bitcoin:" uri scheme
                if (InvoiceUtil.isBitcoinUri(invoice)) {

                    // Add "//" to make it parsable for the java URI class if it is not present
                    if (!invoice.substring(0, 10).equalsIgnoreCase(InvoiceUtil.URI_PREFIX_BITCOIN + "//")) {
                        invoice = InvoiceUtil.URI_PREFIX_BITCOIN + "//" + invoice.substring(8);
                    }

                    URI bitcoinURI = null;
                    try {
                        bitcoinURI = new URI(invoice);

                        mOnChainAddress = bitcoinURI.getHost();


                        String message = null;

                        // Fetch params
                        if (bitcoinURI.getQuery() != null) {
                            String[] valuePairs = bitcoinURI.getQuery().split("&");
                            for (String pair : valuePairs) {
                                String[] param = pair.split("=");
                                if (param[0].equals("amount")) {
                                    mOnChainInvoiceAmount = (long) (Double.parseDouble(param[1]) * 1e8);
                                }
                                if (param[0].equals("message")) {
                                    mOnChainInvoiceMessage = param[1];
                                }
                            }
                        }
                        validateOnChainAddress(mOnChainAddress);

                    } catch (URISyntaxException e) {
                        ZapLog.debug(LOG_TAG, "URI could not be parsed");
                        e.printStackTrace();
                        showError("Error reading the bitcoin invoice", 4000);
                    }

                } else {
                    // We also don't have a bitcoin invoice, check if the is a valid bitcoin address
                    mOnChainAddress = invoice;
                    validateOnChainAddress(mOnChainAddress);
                }


            }


        } else {
            // The wallet is not setup yet, go to next screen to show demo data.
            showError(getResources().getString(R.string.demo_setupWalletFirst), 4000);
        }
    }

    private void validateOnChainAddress(String address) {
        if (address != null) {
            if (Wallet.getInstance().isTestnet()) {
                // We are on testnet
                if (address.startsWith("m") || address.startsWith("n") || address.startsWith("2") || address.toLowerCase().startsWith("tb1")) {
                    goToOnChainPaymentScreen();
                } else if (address.startsWith("1") || address.startsWith("3") || address.toLowerCase().startsWith("bc1")) {
                    // Show error. Please use a TESTNET invoice.
                    showError(getResources().getString(R.string.error_useTestnetRequest), 5000);
                } else {
                    // Show error. No valid payment info.
                    showError(getResources().getString(R.string.error_notAPaymentRequest), 7000);
                }
            } else {
                // We are on mainnet
                if (address.startsWith("1") || address.startsWith("3") || address.toLowerCase().startsWith("bc1")) {
                    goToOnChainPaymentScreen();
                } else if (address.startsWith("m") || address.startsWith("n") || address.startsWith("2") || address.toLowerCase().startsWith("tb1")) {
                    showError(getResources().getString(R.string.error_useMainnetRequest), 5000);
                } else {
                    // Show error. No valid payment info.
                    showError(getResources().getString(R.string.error_notAPaymentRequest), 7000);
                }
            }
        } else {
            // Show error. No valid payment info.
            showError(getResources().getString(R.string.error_notAPaymentRequest), 7000);
        }


    }

    private void goToOnChainPaymentScreen() {
        // Decoded successfully, go to send page.

        Intent intent = new Intent();
        intent.putExtra("onChain", true);
        intent.putExtra("onChainAddress", mOnChainAddress);
        intent.putExtra("onChainAmount", mOnChainInvoiceAmount);
        intent.putExtra("onChainMessage", mOnChainInvoiceMessage);
        setResult(1, intent);
        finish();
    }

    private void decodeLightningInvoice(String invoice) {
        PayReqString decodePaymentRequest = PayReqString.newBuilder()
                .setPayReq(invoice)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().decodePayReq(decodePaymentRequest)
                .timeout(RefConstants.TIMEOUT_SHORT, TimeUnit.SECONDS)
                .subscribe(paymentRequest -> {
                    ZapLog.debug(LOG_TAG, paymentRequest.toString());

                    if (paymentRequest.getTimestamp() + paymentRequest.getExpiry() < System.currentTimeMillis() / 1000) {
                        // Show error: payment request expired.
                        showError(getResources().getString(R.string.error_paymentRequestExpired), 3000);
                    } else if (paymentRequest.getNumSatoshis() == 0) {
                        // Disable 0 sat invoices
                        showError(getResources().getString(R.string.error_notAPaymentRequest), 7000);
                    } else {
                        // Decoded successfully, go to send page.
                        goToLightningPaymentScreen(paymentRequest, invoice);
                    }
                }, throwable -> {
                    // If LND can't decode the payment request, show the error LND throws (always english)
                    runOnUiThread(() -> showError(throwable.getMessage(), 3000));
                    ZapLog.debug(LOG_TAG, throwable.getMessage());
                }));
    }

    private void goToLightningPaymentScreen(PayReq paymentRequest, String invoice) {
        Intent intent = new Intent();
        intent.putExtra("onChain", false);
        intent.putExtra("lnPaymentRequest", paymentRequest.toByteArray());
        intent.putExtra("lnInvoice", invoice);
        setResult(1, intent);
        finish();
    }

}
