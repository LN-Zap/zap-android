package zapsolutions.zap.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.PayReqString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;

public class InvoiceUtil {
    private static final String LOG_TAG = InvoiceUtil.class.getName();

    public static String INVOICE_PREFIX_LIGHTNING_MAINNET = "lnbc";
    public static String INVOICE_PREFIX_LIGHTNING_TESTNET = "lntb";
    public static String INVOICE_PREFIX_LIGHTNING_REGTEST = "lnbcrt";
    public static ArrayList<String> ADDRESS_PREFIX_ONCHAIN_MAINNET = new ArrayList<>(Arrays.asList("1", "3", "bc1"));
    public static ArrayList<String> ADDRESS_PREFIX_ONCHAIN_TESTNET = new ArrayList<>(Arrays.asList("m", "n", "2", "tb1"));
    public static ArrayList<String> ADDRESS_PREFIX_ONCHAIN_REGTEST = new ArrayList<>(Arrays.asList("m", "n", "2", "bcrt1"));
    private static int INVOICE_LIGHTNING_MIN_LENGTH = 6;
    private static int ADDRESS_ON_CHAIN_MIN_LENGTH = 5;


    public static boolean isLightningInvoice(@NonNull String data) {
        if (data.isEmpty() || data.length() < INVOICE_LIGHTNING_MIN_LENGTH) {
            return false;
        }

        return hasPrefix(INVOICE_PREFIX_LIGHTNING_MAINNET, data) || hasPrefix(INVOICE_PREFIX_LIGHTNING_TESTNET, data) || hasPrefix(INVOICE_PREFIX_LIGHTNING_REGTEST, data);
    }

    public static boolean isBitcoinAddress(@NonNull String data) {
        if (data.isEmpty() || data.length() < ADDRESS_ON_CHAIN_MIN_LENGTH) {
            return false;
        }
        ArrayList<String> prefixes = new ArrayList<>();
        prefixes.addAll(ADDRESS_PREFIX_ONCHAIN_MAINNET);
        prefixes.addAll(ADDRESS_PREFIX_ONCHAIN_TESTNET);
        prefixes.addAll(ADDRESS_PREFIX_ONCHAIN_REGTEST);
        return hasPrefix(prefixes, data);
    }

    public static void readInvoice(Context ctx, CompositeDisposable compositeDisposable, String data, OnReadInvoiceCompletedListener listener) {

        // Avoid index out of bounds. An Request with less than 11 characters isn't valid.
        if (data.length() < 11) {
            listener.onNoInvoiceData();
            return;
        }

        // convert to lower case
        String lnInvoice = data.toLowerCase();

        // Remove the "lightning:" uri scheme if it is present, LND needs it without uri scheme
        lnInvoice = UriUtil.removeURI(lnInvoice);

        // Check if the invoice is a lightning invoice
        if (InvoiceUtil.isLightningInvoice(lnInvoice)) {

            // Check if the invoice is for the same network the app is connected to
            switch (Wallet.getInstance().getNetwork()) {
                case MAINNET:
                    if (hasPrefix(INVOICE_PREFIX_LIGHTNING_MAINNET, lnInvoice) && !hasPrefix(INVOICE_PREFIX_LIGHTNING_REGTEST, lnInvoice)) {
                        decodeLightningInvoice(ctx, listener, lnInvoice, compositeDisposable);
                    } else {
                        // Show error. Please use a MAINNET invoice.
                        listener.onError(ctx.getString(R.string.error_useMainnetRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    break;
                case TESTNET:
                    if (hasPrefix(INVOICE_PREFIX_LIGHTNING_TESTNET, lnInvoice)) {
                        decodeLightningInvoice(ctx, listener, lnInvoice, compositeDisposable);
                    } else {
                        // Show error. Please use a TESTNET invoice.
                        listener.onError(ctx.getString(R.string.error_useTestnetRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    break;
                case REGTEST:
                    if (hasPrefix(INVOICE_PREFIX_LIGHTNING_REGTEST, lnInvoice)) {
                        decodeLightningInvoice(ctx, listener, lnInvoice, compositeDisposable);
                    } else {
                        // Show error. Please use a REGTEST invoice.
                        listener.onError(ctx.getString(R.string.error_useRegtestRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
            }
        } else {
            // We do not have a lightning invoice... check if it is a valid bitcoin address / invoice

            // Check if we have a bitcoin invoice with the "bitcoin:" uri scheme
            if (UriUtil.isBitcoinUri(data)) {

                // Add "//" to make it parsable for the java URI class if it is not present
                if (!data.substring(0, 10).equalsIgnoreCase(UriUtil.URI_PREFIX_BITCOIN + "//")) {
                    data = UriUtil.URI_PREFIX_BITCOIN + "//" + data.substring(8);
                }

                URI bitcoinURI = null;
                try {
                    bitcoinURI = new URI(data);

                    String onChainAddress = bitcoinURI.getHost();

                    long onChainInvoiceAmount = 0L;
                    String onChainInvoiceMessage = null;

                    // Fetch params
                    if (bitcoinURI.getQuery() != null) {
                        String[] valuePairs = bitcoinURI.getQuery().split("&");
                        for (String pair : valuePairs) {
                            String[] param = pair.split("=");
                            if (param[0].equals("amount")) {
                                onChainInvoiceAmount = (long) (Double.parseDouble(param[1]) * 1e8);
                            }
                            if (param[0].equals("message")) {
                                onChainInvoiceMessage = param[1];
                            }
                        }
                    }
                    validateOnChainAddress(ctx, listener, onChainAddress, onChainInvoiceAmount, onChainInvoiceMessage);

                } catch (URISyntaxException e) {
                    ZapLog.w(LOG_TAG, "URI could not be parsed");
                    e.printStackTrace();
                    listener.onError(ctx.getString(R.string.error_invalid_bitcoin_request), RefConstants.ERROR_DURATION_MEDIUM);
                }

            } else {
                // We also don't have a bitcoin invoice, check if the data is a valid bitcoin address
                validateOnChainAddress(ctx, listener, data, 0L, null);
            }

        }

    }

    private static void validateOnChainAddress(Context ctx, OnReadInvoiceCompletedListener listener, String address, long amount, String message) {
        if (address != null && isBitcoinAddress(address)) {
            switch (Wallet.getInstance().getNetwork()) {
                case MAINNET:
                    if (hasPrefix(ADDRESS_PREFIX_ONCHAIN_MAINNET, address)) {
                        listener.onValidBitcoinInvoice(address, amount, message);
                    } else {
                        listener.onError(ctx.getString(R.string.error_useMainnetRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    break;
                case TESTNET:
                    if (hasPrefix((ArrayList<String>) ADDRESS_PREFIX_ONCHAIN_TESTNET, address)) {
                        listener.onValidBitcoinInvoice(address, amount, message);
                    } else {
                        listener.onError(ctx.getString(R.string.error_useTestnetRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    break;
                case REGTEST:
                    if (hasPrefix((ArrayList<String>) ADDRESS_PREFIX_ONCHAIN_REGTEST, address)) {
                        listener.onValidBitcoinInvoice(address, amount, message);
                    } else {
                        listener.onError(ctx.getString(R.string.error_useRegtestRequest), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    break;
            }
        } else {
            listener.onNoInvoiceData();
        }
    }

    private static void decodeLightningInvoice(Context ctx, OnReadInvoiceCompletedListener listener, String invoice, CompositeDisposable compositeDisposable) {
        PayReqString decodePaymentRequest = PayReqString.newBuilder()
                .setPayReq(invoice)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().decodePayReq(decodePaymentRequest)
                .timeout(RefConstants.TIMEOUT_SHORT * TorUtil.getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paymentRequest -> {
                    ZapLog.v(LOG_TAG, paymentRequest.toString());

                    if (paymentRequest.getTimestamp() + paymentRequest.getExpiry() < System.currentTimeMillis() / 1000) {
                        // Show error: payment request expired.
                        listener.onError(ctx.getString(R.string.error_paymentRequestExpired), RefConstants.ERROR_DURATION_SHORT);
                    } else if (paymentRequest.getNumSatoshis() == 0) {
                        // Disable 0 sat invoices
                        listener.onError(ctx.getString(R.string.error_notAPaymentRequest), RefConstants.ERROR_DURATION_LONG);
                    } else {
                        listener.onValidLightningInvoice(paymentRequest, invoice);
                    }
                }, throwable -> {
                    // If LND can't decode the payment request, show the error LND throws (always english)
                    listener.onError(throwable.getMessage(), RefConstants.ERROR_DURATION_MEDIUM);

                    ZapLog.d(LOG_TAG, throwable.getMessage());
                }));

    }

    private static boolean hasPrefix(@NonNull String prefix, @NonNull String data) {
        if (data.isEmpty() || data.length() < prefix.length()) {
            return false;
        }

        return data.substring(0, prefix.length()).equalsIgnoreCase(prefix);
    }

    private static boolean hasPrefix(@NonNull ArrayList<String> prefixes, @NonNull String data) {
        if (data.isEmpty()) {
            return false;
        }
        boolean hasPrefix = false;
        for (String prefix : prefixes) {
            if (data.length() >= prefix.length()) {
                if (data.substring(0, prefix.length()).equalsIgnoreCase(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface OnReadInvoiceCompletedListener {
        void onValidLightningInvoice(PayReq paymentRequest, String invoice);

        void onValidBitcoinInvoice(String address, long amount, String message);

        void onError(String error, int duration);

        void onNoInvoiceData();
    }
}
