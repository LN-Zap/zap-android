package zapsolutions.zap.walletManagement;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;

public class WalletItem  {
    private WalletConfig mWalletConfig;

    public WalletItem(WalletConfig walletConfig) {
        mWalletConfig = walletConfig;
    }

    public WalletConfig getWalletConfig() {
        return mWalletConfig;
    }
}
