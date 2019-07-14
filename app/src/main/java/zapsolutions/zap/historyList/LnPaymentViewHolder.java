package zapsolutions.zap.historyList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.PayReqString;
import com.google.common.util.concurrent.ListenableFuture;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.LndConnection;
import zapsolutions.zap.util.ExecuteOnCaller;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.ZapLog;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LnPaymentViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "LnPaymentViewHolder";

    private ImageView mIcon;
    private TextView mTimeOfDay;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mTransactionFee;
    private View mRootView;
    private Context mContext;

    public LnPaymentViewHolder(View v) {
        super(v);

        mIcon = v.findViewById(R.id.transactionTypeIcon);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mTransactionState = v.findViewById(R.id.transactionState);
        mDescription = v.findViewById(R.id.transactionDescription);
        mAmount = v.findViewById(R.id.transactionAmount);
        mTransactionFee = v.findViewById(R.id.transactionFeeAmount);
        mRootView = v.findViewById(R.id.transactionRootView);
        mContext = v.getContext();
    }

    public void bindLnPaymentItem(LnPaymentItem lnPaymentItem) {

        // Set Icon
        mIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
        mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(lnPaymentItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set state
        mTransactionState.setText(R.string.sent);

        // Set amount
        long amt = lnPaymentItem.getPayment().getValueSat();
        mAmount.setText("- " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));

        // Set fee
        long feeAmt = lnPaymentItem.getPayment().getFee();

        String feeText = mContext.getResources().getString(R.string.fee)
                + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
        mTransactionFee.setText(feeText);

        // Set on click listener
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Toast.makeText(mContext, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            }
        });


        // Set description
        mDescription.setVisibility(View.GONE);

        if (!lnPaymentItem.getPayment().getPaymentRequest().isEmpty()) {
            // This will only be true for payments done with LND 0.7.0-beta and later
            decodeLightningInvoice(lnPaymentItem.getPayment().getPaymentRequest());
        }

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
                        mDescription.setVisibility(View.VISIBLE);
                        mDescription.setText(paymentRequest.getDescription());
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
