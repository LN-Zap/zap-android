package zapsolutions.zap.transactionHistory.listItems;

import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import zapsolutions.zap.R;
import zapsolutions.zap.util.MonetaryUtil;


/**
 * This class can be used to generate imaginary transactions for demonstration purposes.
 */
public class DemoViewHolder extends TransactionViewHolder {
    // each data item is just a string in this case
    private TextView mAmount;

    public DemoViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
    }

    public void bindDemoItem(DemoItem demoItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        setSuccessState(true);

        mAmount.setVisibility(View.VISIBLE);

        // Set time of Day
        setTimeOfDay(demoItem.mCreationDate);

        // Set description
        if (demoItem.getDescription().equals("")) {
            setSecondaryDescription("", false);
        } else {
            setSecondaryDescription(demoItem.getDescription(), true);
        }

        switch (demoItem.getTransactionType()) {
            case HistoryListItem.TYPE_ON_CHAIN_TRANSACTION:
                if (demoItem.isInternal()) {
                    setIcon(TransactionIcon.INTERNAL);
                    setFee(demoItem.getFee(), false);
                    if (demoItem.getAmount() > 0) {
                        setPrimaryDescription(mContext.getString(R.string.closed_channel));
                        setAmount(demoItem.getAmount(), false);
                    } else {
                        setPrimaryDescription(mContext.getString(R.string.opened_channel));
                        setAmount(demoItem.getAmount(), true);
                    }
                } else {
                    setIcon(TransactionIcon.ONCHAIN);
                    setAmount(demoItem.getAmount(), true);
                    if (demoItem.getAmount() > 0) {
                        setPrimaryDescription(mContext.getString(R.string.received));
                        setFee(demoItem.getFee(), false);
                    } else {
                        setPrimaryDescription(mContext.getString(R.string.sent));
                        setFee(demoItem.getFee(), true);
                    }
                }
                break;
            case HistoryListItem.TYPE_LN_INVOICE:
                setIcon(TransactionIcon.PENDING);
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                mAmount.setVisibility(View.VISIBLE);
                setFee(demoItem.getFee(), false);
                if (demoItem.isExpired()) {
                    setPrimaryDescription(mContext.getString(R.string.request_expired));
                    setSuccessState(false);
                } else {
                    setPrimaryDescription(mContext.getString(R.string.requested_payment));
                }
                break;
            case HistoryListItem.TYPE_LN_PAYMENT:
                setIcon(TransactionIcon.LIGHTNING);
                setAmount(demoItem.getAmount(), true);
                if (demoItem.getAmount() > 0) {
                    setPrimaryDescription(mContext.getString(R.string.received));
                    setFee(demoItem.getFee(), false);
                } else {
                    setPrimaryDescription(mContext.getString(R.string.sent));
                    setFee(demoItem.getFee(), true);
                }
                break;
        }

        // Set on click listener
        setOnRootViewClickListener(demoItem, HistoryListItem.TYPE_DEMO);
    }
}
