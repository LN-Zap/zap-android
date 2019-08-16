package zapsolutions.zap.transactionHistory.listItems;

import com.google.protobuf.ByteString;

/**
 * This class can be used to generate imaginary transactions for demonstration purposes.
 */
public class DemoItem extends TransactionItem {


    private int mTransactionType; // 1 - on-chain tx, 2 - lnInvoice, 3 - lnPayment
    private String mDescription;
    private long mAmount;
    private long mFee;

    private boolean mInternal = false;
    private boolean mExpired = false;

    public DemoItem(int txType, String description, long date, long amount, long fee, boolean internal, boolean expired) {
        mTransactionType = txType;
        mDescription = description;
        mCreationDate = date;
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

    @Override
    public ByteString getTransactionByteString() {
        return null;
    }
}
