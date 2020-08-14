package zapsolutions.zap.transactionHistory.listItems;

import android.content.SharedPreferences;
import android.view.View;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;

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

        if (lnPaymentItem.getMemo() == null) {
            setSecondaryDescription("", false);
        } else {
            setSecondaryDescription(lnPaymentItem.getMemo(), true);
        }

        // Set on click listener
        setOnRootViewClickListener(lnPaymentItem, HistoryListItem.TYPE_LN_PAYMENT);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("firstCurrencyIsPrimary")) {
            bindLnPaymentItem(mLnPaymentItem);
        }
    }
}
