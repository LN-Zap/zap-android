package zapsolutions.zap.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BalancesTest {

    @Test
    public void testBalanceTotal() {
        long onChainBalanceTotal = 200;
        long onChainBalanceConfirmed = 100;
        long onChainBalanceUnconfirmed = 100;
        long channelBalance = 100;
        long channelBalancePendingOpen = 100;
        long channelBalanceLimbo = 100;

        Balances balances = new Balances(onChainBalanceTotal,
                onChainBalanceConfirmed,
                onChainBalanceUnconfirmed,
                channelBalance,
                channelBalancePendingOpen,
                channelBalanceLimbo);

        assertEquals(500, balances.total());
    }
}
