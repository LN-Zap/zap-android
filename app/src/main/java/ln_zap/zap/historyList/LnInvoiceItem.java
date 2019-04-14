package ln_zap.zap.historyList;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;

public class LnInvoiceItem extends HistoryListItem {
    private Invoice mInvoice;

    public LnInvoiceItem(Invoice invoice) {
        mInvoice = invoice;
        mCreationDate = invoice.getCreationDate();
    }

    @Override
    public int getType() {
        return TYPE_LN_INVOICE;
    }

    public Invoice getInvoice() {
        return mInvoice;
    }
}
