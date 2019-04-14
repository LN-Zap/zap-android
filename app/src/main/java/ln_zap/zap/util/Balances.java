package ln_zap.zap.util;

/**
 * This class helps to organize the various types of balances.
 */
public class Balances {
    private long mOnChainBalanceTotal = 0;
    private long mOnChainBalanceConfirmed = 0;
    private long mOnChainBalanceUnconfirmed = 0;
    private long mChannelBalance = 0;
    private long mChannelBalancePending = 0;

    public Balances(long onChainTotal, long onChainConfirmed,
                    long onChainUnconfirmed, long channelBalance,
                    long channelBalancePending) {
        mOnChainBalanceTotal = onChainTotal;
        mOnChainBalanceConfirmed = onChainConfirmed;
        mOnChainBalanceUnconfirmed = onChainUnconfirmed;
        mChannelBalance = channelBalance;
        mChannelBalancePending = channelBalancePending;
    }

    public long total() {
        return mOnChainBalanceTotal + mChannelBalance + mChannelBalancePending;
    }

    public long onChainTotal() {
        return mOnChainBalanceTotal;
    }

    public long onChainConfirmed() {
        return mOnChainBalanceConfirmed;
    }

    public long onChainUnconfirmed() {
        return mOnChainBalanceUnconfirmed;
    }

    public long channelBalance() {
        return mChannelBalance;
    }

    public long channelBalancePending() {
        return mChannelBalancePending;
    }
}
