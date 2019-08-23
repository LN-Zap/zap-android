package zapsolutions.zap.transactionHistory.listItems;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.google.protobuf.ByteString;

public class LnInvoiceItem extends TransactionItem {
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

    @Override
    public ByteString getTransactionByteString() {
        return mInvoice.toByteString();
    }
}
