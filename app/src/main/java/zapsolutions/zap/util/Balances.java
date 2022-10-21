package zapsolutions.zap.util;

/**
 * This class helps to organize the various types of balances.
 */
public class Balances {
    private final long mOnChainBalanceTotal;
    private final long mOnChainBalanceConfirmed;
    private final long mOnChainBalanceUnconfirmed;
    private final long mChannelBalance;
    private final long mChannelBalancePendingOpen;
    private final long mChannelBalanceLimbo;

    public Balances(long onChainTotal, long onChainConfirmed,
                    long onChainUnconfirmed, long channelBalance,
                    long channelBalancePendingOpen, long channelBalanceLimbo) {
        mOnChainBalanceTotal = onChainTotal;
        mOnChainBalanceConfirmed = onChainConfirmed;
        mOnChainBalanceUnconfirmed = onChainUnconfirmed;
        mChannelBalance = channelBalance;
        mChannelBalancePendingOpen = channelBalancePendingOpen;
        mChannelBalanceLimbo = channelBalanceLimbo;
    }

    public long total() {
        return mOnChainBalanceTotal + mChannelBalance + mChannelBalancePendingOpen + mChannelBalanceLimbo;
    }

    public long channelTotal() {
        return mChannelBalance + mChannelBalancePendingOpen + mChannelBalanceLimbo;
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
        return mChannelBalancePendingOpen;
    }

    public long channelBalanceLimbo() {
        return mChannelBalanceLimbo;
    }
}
