package ln_zap.zap.historyList;

import com.github.lightningnetwork.lnd.lnrpc.Transaction;

public class TransactionItem extends HistoryListItem {
    private Transaction mOnChainTransaction;

    public TransactionItem(Transaction onChainTransaction){
        mOnChainTransaction = onChainTransaction;
        mCreationDate = onChainTransaction.getTimeStamp();
    }

    @Override
    public int getType() {
        return TYPE_TRANSACTION;
    }

    public Transaction getOnChainTransaction() {
        return mOnChainTransaction;
    }
}
