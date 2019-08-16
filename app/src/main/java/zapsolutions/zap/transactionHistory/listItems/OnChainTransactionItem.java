package zapsolutions.zap.transactionHistory.listItems;

import com.github.lightningnetwork.lnd.lnrpc.Transaction;
import com.google.protobuf.ByteString;

public class OnChainTransactionItem extends TransactionItem {
    private Transaction mOnChainTransaction;

    public OnChainTransactionItem(Transaction onChainTransaction) {
        mOnChainTransaction = onChainTransaction;
        mCreationDate = onChainTransaction.getTimeStamp();
    }

    @Override
    public int getType() {
        return TYPE_ON_CHAIN_TRANSACTION;
    }

    public Transaction getOnChainTransaction() {
        return mOnChainTransaction;
    }

    @Override
    public ByteString getTransactionByteString() {
        return mOnChainTransaction.toByteString();
    }
}
