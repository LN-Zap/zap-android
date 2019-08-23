package zapsolutions.zap.transactionHistory.listItems;

import android.view.View;
import zapsolutions.zap.R;
import zapsolutions.zap.util.Wallet;


public class LnInvoiceViewHolder extends TransactionViewHolder {


    public LnInvoiceViewHolder(View v) {
        super(v);
    }

    public void bindLnInvoiceItem(LnInvoiceItem lnInvoiceItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        setSuccessState(true);

        setFee(0, false);
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
                setAmount(0L, R.color.gray, false, true);

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
                setAmount(amt, R.color.gray, true, true);

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
