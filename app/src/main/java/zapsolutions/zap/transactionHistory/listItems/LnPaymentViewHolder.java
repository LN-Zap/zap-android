package zapsolutions.zap.transactionHistory.listItems;

import android.view.View;

import com.github.lightningnetwork.lnd.lnrpc.Hop;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.contacts.ContactsManager;

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
        setDisplayMode(true);

        Hop lastHop = lnPaymentItem.getPayment().getHtlcs(0).getRoute().getHops(lnPaymentItem.getPayment().getHtlcs(0).getRoute().getHopsCount() - 1);
        String payee = lastHop.getPubKey();

        String payeeName = ContactsManager.getInstance().getNameByContactData(payee);
        if (payee.equals(payeeName)) {
            setPrimaryDescription(mContext.getResources().getString(R.string.sent));
        } else {
            setPrimaryDescription(payeeName);
        }

        setIcon(TransactionIcon.LIGHTNING);
        setTimeOfDay(lnPaymentItem.mCreationDate);
        setAmount(lnPaymentItem.getPayment().getValueSat() * -1, true);
        setFee(lnPaymentItem.getPayment().getFee(), true);

        if (lnPaymentItem.getMemo() == null || lnPaymentItem.getMemo().isEmpty()) {
            setSecondaryDescription("", false);
        } else {
            setSecondaryDescription(lnPaymentItem.getMemo(), true);
        }

        // Set on click listener
        setOnRootViewClickListener(lnPaymentItem, HistoryListItem.TYPE_LN_PAYMENT);
    }

    @Override
    public void refreshViewHolder() {
        bindLnPaymentItem(mLnPaymentItem);
        super.refreshViewHolder();
    }
}
