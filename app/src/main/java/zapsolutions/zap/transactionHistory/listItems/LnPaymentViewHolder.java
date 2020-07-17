package zapsolutions.zap.transactionHistory.listItems;

import android.content.SharedPreferences;
import android.view.View;

import com.github.lightningnetwork.lnd.lnrpc.PayReqString;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.util.ZapLog;

public class LnPaymentViewHolder extends TransactionViewHolder {

    private static final String LOG_TAG = LnPaymentViewHolder.class.getName();

    private CompositeDisposable mCompositeDisposable;
    private LnPaymentItem mLnPaymentItem;

    public LnPaymentViewHolder(View v) {
        super(v);
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        mCompositeDisposable = compositeDisposable;
    }

    public void bindLnPaymentItem(LnPaymentItem lnPaymentItem) {
        mLnPaymentItem = lnPaymentItem;

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
        PayReqString decodePaymentRequest = PayReqString.newBuilder()
                .setPayReq(invoice)
                .build();

        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed())
            mCompositeDisposable.add(LndConnection.getInstance().getLightningService().decodePayReq(decodePaymentRequest)
                    .subscribe(payReq -> {
                        if (!payReq.getDescription().isEmpty()) {
                            // Set description
                            setSecondaryDescription(payReq.getDescription(), true);
                        }
                    }, throwable -> ZapLog.debug(LOG_TAG, "Decode payment request failed: " + throwable.fillInStackTrace())));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("firstCurrencyIsPrimary")) {
            bindLnPaymentItem(mLnPaymentItem);
        }
    }
}
