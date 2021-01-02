package zapsolutions.zap.transactionHistory.listItems;

public abstract class HistoryListItem implements Comparable<HistoryListItem> {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_ON_CHAIN_TRANSACTION = 1;
    public static final int TYPE_LN_INVOICE = 2;
    public static final int TYPE_LN_PAYMENT = 3;

    public long mCreationDate = 0;

    abstract public int getType();

    public boolean equalsWithSameContent(Object o) {
        if (!equals(o)) {
            return false;
        }

        switch (this.getType()) {
            case TYPE_ON_CHAIN_TRANSACTION:
                int thisConfs = ((OnChainTransactionItem) this).getOnChainTransaction().getNumConfirmations();
                int oConfs = ((OnChainTransactionItem) o).getOnChainTransaction().getNumConfirmations();
                return thisConfs == oConfs;
            case TYPE_LN_INVOICE:
                return ((LnInvoiceItem) this).getInvoice().getStateValue() == ((LnInvoiceItem) o).getInvoice().getStateValue();
            case TYPE_LN_PAYMENT:
                return ((LnPaymentItem) this).getPayment().getStatusValue() == ((LnPaymentItem) o).getPayment().getStatusValue();
            default:
                return true;
        }
    }

    @Override
    public int compareTo(HistoryListItem o) {
        HistoryListItem other = (HistoryListItem) o;
        return Long.compare(other.mCreationDate, this.mCreationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryListItem that = (HistoryListItem) o;

        if (this.getType() != that.getType()) {
            return false;
        }

        switch (this.getType()) {
            case TYPE_ON_CHAIN_TRANSACTION:
                return ((OnChainTransactionItem) this).getOnChainTransaction().getTxHash().equals(((OnChainTransactionItem) that).getOnChainTransaction().getTxHash());
            case TYPE_LN_INVOICE:
                return ((LnInvoiceItem) this).getInvoice().getAddIndex() == ((LnInvoiceItem) that).getInvoice().getAddIndex();
            case TYPE_LN_PAYMENT:
                return ((LnPaymentItem) this).getPayment().getPaymentIndex() == ((LnPaymentItem) that).getPayment().getPaymentIndex();
            default:
                return mCreationDate == that.mCreationDate;
        }
    }

    @Override
    public int hashCode() {
        switch (this.getType()) {
            case TYPE_ON_CHAIN_TRANSACTION:
                return ((OnChainTransactionItem) this).getOnChainTransaction().getTxHash().hashCode();
            case TYPE_LN_INVOICE:
                return Long.valueOf(((LnInvoiceItem) this).getInvoice().getAddIndex()).hashCode();
            case TYPE_LN_PAYMENT:
                return Long.valueOf(((LnPaymentItem) this).getPayment().getPaymentIndex()).hashCode();
            default:
                return Long.valueOf(mCreationDate).hashCode();
        }
    }
}
