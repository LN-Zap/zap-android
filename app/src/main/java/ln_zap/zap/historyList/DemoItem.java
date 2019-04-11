package ln_zap.zap.historyList;


public class DemoItem extends HistoryListItem {


    private int mTransactionType; // 1 - on-chain tx, 2 - lnInvoice, 3 - lnPayment
    private String mDescription;
    private long mDate;
    private long mAmount;
    private long mFee;

    private boolean mInternal = false;
    private boolean mExpired = false;

    public DemoItem(int txType, String description, long date, long amount, long fee, boolean internal, boolean expired) {
        mTransactionType = txType;
        mDescription = description;
        mDate = date;
        mAmount = amount;
        mFee = fee;
        mInternal = internal;
        mExpired = expired;
    }

    @Override
    public int getType() {
        return TYPE_DEMO;
    }

    public int getTransactionType() {
        return mTransactionType;
    }

    public String getDescription() {
        return mDescription;
    }

    public long getDate() {
        return mDate;
    }

    public long getAmount() {
        return mAmount;
    }

    public long getFee() {
        return mFee;
    }

    public boolean isInternal() {
        return mInternal;
    }

    public boolean isExpired() {
        return mExpired;
    }
}
