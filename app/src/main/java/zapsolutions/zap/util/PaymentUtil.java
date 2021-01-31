package zapsolutions.zap.util;

import com.github.lightningnetwork.lnd.lnrpc.Failure;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.PaymentFailureReason;
import com.github.lightningnetwork.lnd.lnrpc.Route;
import com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest;
import com.github.lightningnetwork.lnd.routerrpc.SendToRouteRequest;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;

import java.security.SecureRandom;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.lndConnection.LndConnection;

public class PaymentUtil {

    private static final String LOG_TAG = PaymentUtil.class.getName();


    public static SendPaymentRequest preparePaymentProbe(PayReq paymentRequest) {
        return preparePaymentProbe(paymentRequest.getDestination(), paymentRequest.getNumSatoshis());
    }

    public static SendPaymentRequest preparePaymentProbe(String destination, long amountSat) {
        // The paymentHash will be replaced with a random hash. This way we can create a fake payment.
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        long feeLimit = calculateAbsoluteFeeLimit(amountSat);

        return SendPaymentRequest.newBuilder()
                .setDest(byteStringFromHex(destination))
                .setAmt(amountSat)
                .setFeeLimitSat(feeLimit)
                .setPaymentHash(ByteString.copyFrom(bytes))
                .setNoInflightUpdates(true)
                .setTimeoutSeconds(RefConstants.TIMEOUT_MEDIUM * RefConstants.TOR_TIMEOUT_MULTIPLIER)
                .setMaxParts(1) // We are looking for a direct path. Probing using MPP isn’t really possible at the moment.
                .build();
    }

    /**
     * With a payment probe we test if a route can be found. (no multi path payments)
     * <p>
     * A payment probe is basically a normal transaction with a faked payment hash.
     * If a route is found, then the exact fee and route can be extracted from that payment probe.
     * This way we can show the user the exact amount of fee necessary and pay later by using SendToRouteV2.
     * If a route can’t be found, we present the user “up to xxx sats” where xxx is a fee limit that is configurable via user settings.
     * In this case we fall back to a multi path payment as a last try to get the payment through.
     *
     * @param probeSendRequest    A request created with the preparePaymentProbe function
     * @param compositeDisposable CompositeDisposable the async action gets executed on
     * @param result              OnPaymentProbeResult interface
     */
    public static void sendPaymentProbe(SendPaymentRequest probeSendRequest, CompositeDisposable compositeDisposable, OnPaymentProbeResult result) {
        if (probeSendRequest == null)
            result.onError("Probe send request was null", RefConstants.ERROR_DURATION_MEDIUM);

        ZapLog.d(LOG_TAG, "Sending payment probe...");

        compositeDisposable.add(LndConnection.getInstance().getRouterService().sendPaymentV2(probeSendRequest)
                .subscribe(payment -> {
                    ZapLog.v(LOG_TAG, payment.toString());

                    switch (payment.getFailureReason()) {
                        case FAILURE_REASON_INCORRECT_PAYMENT_DETAILS:
                            Route route = payment.getHtlcs(0).getRoute();
                            long feeSats = 0;
                            if (route.getTotalFeesMsat() % 1000 == 0) {
                                feeSats = route.getTotalFeesMsat() / 1000;
                            } else {
                                feeSats = (route.getTotalFeesMsat() / 1000) + 1;
                            }
                            result.onSuccess(feeSats, route, probeSendRequest.getAmt());
                            break;
                        case FAILURE_REASON_NO_ROUTE:
                            result.onNoRoute(probeSendRequest.getAmt());
                            break;
                        default:
                            result.onError(payment.getFailureReason().toString(), RefConstants.ERROR_DURATION_MEDIUM);
                    }

                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Exception while executing payment probe.");
                    ZapLog.e(LOG_TAG, throwable.getMessage());

                    result.onError(throwable.getMessage(), RefConstants.ERROR_DURATION_MEDIUM);
                }));
    }

    /**
     * Used to send a payment through a predefined route.
     * To determine a route use the sendPaymentProbe function.
     *
     * @param paymentHash         The payment hash for the payment
     * @param route               The route to take
     * @param compositeDisposable CompositeDisposable the async action gets executed on
     * @param result              OnLightningPaymentResult interface
     */
    public static void sendToRoute(String paymentHash, Route route, CompositeDisposable compositeDisposable, OnSendToRouteResult result) {
        SendToRouteRequest sendToRouteRequest = SendToRouteRequest.newBuilder()
                .setPaymentHash(byteStringFromHex(paymentHash))
                .setRoute(route)
                .build();

        ZapLog.d(LOG_TAG, "Trying to send lightning over specific route...");

        compositeDisposable.add(LndConnection.getInstance().getRouterService().sendToRouteV2(sendToRouteRequest)
                .subscribe(htlcAttempt -> {
                    ZapLog.v(LOG_TAG, htlcAttempt.toString());

                    switch (htlcAttempt.getStatus()) {
                        case SUCCEEDED:
                            // updated the history, so it is shown the next time the user views it
                            Wallet.getInstance().updateLightningPaymentHistory();
                            result.onSuccess();
                            break;
                        case FAILED:
                            result.onError(htlcAttempt.getFailure().getCode().toString(), htlcAttempt.getFailure(), RefConstants.ERROR_DURATION_MEDIUM);
                            break;
                    }
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Exception while executing SendToRoute.");
                    ZapLog.e(LOG_TAG, throwable.getMessage());

                    result.onError(throwable.getMessage(), null, RefConstants.ERROR_DURATION_MEDIUM);
                }));
    }

    public static SendPaymentRequest prepareMPPPayment(PayReq paymentRequest, String invoice) {
        long feeLimit = calculateAbsoluteFeeLimit(paymentRequest.getNumSatoshis());
        return SendPaymentRequest.newBuilder()
                .setPaymentRequest(invoice)
                .setFeeLimitSat(feeLimit)
                .setTimeoutSeconds(RefConstants.TIMEOUT_MEDIUM * RefConstants.TOR_TIMEOUT_MULTIPLIER)
                .setMaxParts(RefConstants.LN_MAX_PARTS)
                .build();
    }

    /**
     * Used to send a lightning payment with multi paths enabled.
     *
     * @param sendPaymentRequest  A request created with the prepareMPPPayment function
     * @param compositeDisposable CompositeDisposable the async action gets executed on
     * @param result              OnLightningPaymentResult interface
     */
    public static void sendMppPayment(SendPaymentRequest sendPaymentRequest, CompositeDisposable compositeDisposable, OnMPPPaymentResult result) {
        if (sendPaymentRequest == null)
            result.onError("SendPaymentRequest was null", null, RefConstants.ERROR_DURATION_MEDIUM);

        ZapLog.d(LOG_TAG, "Trying to send mpp lightning payment...");

        ZapLog.v(LOG_TAG, "The settings for the payment are:\n" + sendPaymentRequest.toString());

        compositeDisposable.add(LndConnection.getInstance().getRouterService().sendPaymentV2(sendPaymentRequest)
                .subscribe(payment -> {
                    ZapLog.v(LOG_TAG, payment.toString());

                    switch (payment.getStatus()) {
                        case SUCCEEDED:
                            // updated the history, so it is shown the next time the user views it
                            Wallet.getInstance().updateLightningPaymentHistory();
                            result.onSuccess();
                            break;
                        case FAILED:
                            result.onError(payment.getFailureReason().toString(), payment.getFailureReason(), RefConstants.ERROR_DURATION_MEDIUM);
                            break;
                    }
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Exception in mpp lightning payment task.");
                    result.onError(throwable.getMessage(), null, RefConstants.ERROR_DURATION_MEDIUM);
                }));
    }

    // ByteString values when using for example "paymentRequest.getDescriptionBytes()" can for some reason not directly be used as they are double in length
    private static ByteString byteStringFromHex(String hexString) {
        byte[] hexBytes = BaseEncoding.base16().decode(hexString.toUpperCase());
        return ByteString.copyFrom(hexBytes);
    }

    /**
     * We always allow a fee of at least 3 sats, to ensure also small payments have a chance.
     * For payments of over 100 sat we apply the user settings, for payments lower, we use the square root of the amount to send.
     *
     * @param amountSatToSend Amount that should be send with the transaction
     * @return maixmum number of sats in fee
     */
    public static long calculateAbsoluteFeeLimit(long amountSatToSend) {
        long absFee;
        if (amountSatToSend <= RefConstants.LN_PAYMENT_FEE_THRESHOLD) {
            absFee = (long) (Math.sqrt(amountSatToSend));
        } else {
            absFee = (long) (getRelativeSettingsFeeLimit() * amountSatToSend);
        }
        return Math.max(absFee, 3L);
    }

    public static float getRelativeSettingsFeeLimit() {
        String lightning_feeLimit = PrefsUtil.getPrefs().getString("lightning_feeLimit", "3%");
        String feePercent = lightning_feeLimit.replace("%", "");
        float feeMultiplier = 1f;
        if (!feePercent.equals(App.getAppContext().getResources().getString(R.string.none))) {
            feeMultiplier = Integer.parseInt(feePercent) / 100f;
        }
        return feeMultiplier;
    }

    public static float calculateRelativeFeeLimit(long amountSatToSend) {
        return (float) calculateAbsoluteFeeLimit(amountSatToSend) / (float) amountSatToSend;
    }

    public interface OnPaymentProbeResult {
        void onSuccess(long fee, Route route, long paymentAmountSat);

        void onNoRoute(long paymentAmountSat);

        void onError(String error, int duration);
    }

    public interface OnSendToRouteResult {
        void onSuccess();

        void onError(String error, Failure reason, int duration);
    }

    public interface OnMPPPaymentResult {
        void onSuccess();

        void onError(String error, PaymentFailureReason reason, int duration);
    }
}
