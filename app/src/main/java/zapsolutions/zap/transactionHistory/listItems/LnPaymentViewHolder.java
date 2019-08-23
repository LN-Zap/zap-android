package zapsolutions.zap.transactionHistory.listItems;

import android.view.View;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.PayReqString;
import com.google.common.util.concurrent.ListenableFuture;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.util.ExecuteOnCaller;
import zapsolutions.zap.util.ZapLog;

import java.util.concurrent.ExecutionException;

public class LnPaymentViewHolder extends TransactionViewHolder {

    private static final String LOG_TAG = LnPaymentViewHolder.class.getName();

    public LnPaymentViewHolder(View v) {
        super(v);
    }

    public void bindLnPaymentItem(LnPaymentItem lnPaymentItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        setSuccessState(true);

        setIcon(TransactionIcon.LIGHTNING);
        setTimeOfDay(lnPaymentItem.mCreationDate);
        setPrimaryDescription(mContext.getResources().getString(R.string.sent));
        setAmount(lnPaymentItem.getPayment().getValueSat() * -1, true);
        setFee(lnPaymentItem.getPayment().getFee(), true);
        setSecondaryDescription("", false);

        if (!lnPaymentItem.getPayment().getPaymentRequest().isEmpty()) {
            // This will only be true for payments done with LND 0.7.0-beta and later
            decodeLightningInvoice(lnPaymentItem.getPayment().getPaymentRequest());
        }

        // Set on click listener
        setOnRootViewClickListener(lnPaymentItem, HistoryListItem.TYPE_LN_PAYMENT);
    }

    private void decodeLightningInvoice(String invoice) {

        // decode lightning invoice
        LightningGrpc.LightningFutureStub asyncPayReqClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        PayReqString decodePaymentRequest = PayReqString.newBuilder()
                .setPayReq(invoice)
                .build();

        final ListenableFuture<PayReq> payReqFuture = asyncPayReqClient.decodePayReq(decodePaymentRequest);

        payReqFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    PayReq paymentRequest = payReqFuture.get();

                    if (!paymentRequest.getDescription().isEmpty()) {
                        // Set description
                        setSecondaryDescription(paymentRequest.getDescription(), true);
                    }

                    // ZapLog.debug(LOG_TAG, String.valueOf(paymentsResponse.toString()));
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Decode payment request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in decode payment request task.");
                }
            }
        }, new ExecuteOnCaller());
    }

}
