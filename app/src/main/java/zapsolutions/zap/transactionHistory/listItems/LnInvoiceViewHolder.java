package zapsolutions.zap.transactionHistory.listItems;

import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import zapsolutions.zap.R;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.Wallet;


public class LnInvoiceViewHolder extends TransactionViewHolder {

    private TextView mAmount;

    public LnInvoiceViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
    }

    public void bindLnInvoiceItem(LnInvoiceItem lnInvoiceItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        setSuccessState(true);

        // Set fee
        setFee(0, false);

        // Set time of Day
        setTimeOfDay(lnInvoiceItem.mCreationDate);

        // Set description
        if (lnInvoiceItem.getInvoice().getMemo().equals("")) {
            setSecondaryDescription("", false);
        } else {
            setSecondaryDescription(lnInvoiceItem.getInvoice().getMemo(), true);
        }


        Long amt = lnInvoiceItem.getInvoice().getValue();
        Long amtPayed = lnInvoiceItem.getInvoice().getAmtPaidSat();

        if (amt.equals(0L)) {
            // if no specific value was requested
            if (!amtPayed.equals(0L)) {

                // The invoice has been payed
                setIcon(TransactionIcon.LIGHTNING);
                setPrimaryDescription(mContext.getString(R.string.received));
                setAmount(amtPayed, true);

            } else {

                // The invoice has not been payed yet
                setIcon(TransactionIcon.PENDING);
                mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                mAmount.setVisibility(View.VISIBLE);

                if (Wallet.getInstance().isInvoiceExpired(lnInvoiceItem.getInvoice())) {

                    // The invoice has expired
                    setPrimaryDescription(mContext.getString(R.string.request_expired));
                    setSuccessState(false);

                } else {

                    // The invoice has not yet expired
                    setPrimaryDescription(mContext.getString(R.string.requested_payment));

                }
            }

        } else {

            // if a specific value was requested
            if (amtPayed.equals(amt)) {

                // The invoice has been payed
                setIcon(TransactionIcon.LIGHTNING);
                setPrimaryDescription(mContext.getString(R.string.received));
                setAmount(amt, true);

            } else {

                // The invoice has not been payed yet
                setIcon(TransactionIcon.PENDING);
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                mAmount.setVisibility(View.VISIBLE);

                if (Wallet.getInstance().isInvoiceExpired(lnInvoiceItem.getInvoice())) {

                    // The invoice has expired
                    setPrimaryDescription(mContext.getString(R.string.request_expired));
                    setSuccessState(false);

                } else {

                    // The invoice has not yet expired
                    setPrimaryDescription(mContext.getString(R.string.requested_payment));

                }
            }
        }

        // Set on click listener
        setOnRootViewClickListener(lnInvoiceItem, HistoryListItem.TYPE_LN_INVOICE);
    }
}
