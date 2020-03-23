package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class WalletConfigsJson {

    @SerializedName("connections")
    Set<WalletConfig> mConnections;

    public WalletConfig getConnectionById(@NonNull String id) {
        for (WalletConfig walletConnectionConfig : mConnections) {
            if (walletConnectionConfig.getId().equals(id)) {
                return walletConnectionConfig;
            }
        }
        return null;
    }

    public WalletConfig getConnectionByAlias(@NonNull String alias) {
        for (WalletConfig walletConnectionConfig : mConnections) {
            if (walletConnectionConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                return walletConnectionConfig;
            }
        }
        return null;
    }

    public Set<WalletConfig> getConnections() {
        return mConnections;
    }

    boolean doesWalletConfigExist(@NonNull WalletConfig walletConfig) {
        return mConnections.contains(walletConfig);
    }

    boolean addWallet(@NonNull WalletConfig walletConfig) {
        return mConnections.add(walletConfig);
    }

    boolean removeWalletConfig(WalletConfig walletConfig) {
        return mConnections.remove(walletConfig);
    }

    boolean updateWalletConfig(WalletConfig walletConfig) {
        if (doesWalletConfigExist(walletConfig)) {
            mConnections.remove(walletConfig);
            mConnections.add(walletConfig);
            return true;
        } else {
            return false;
        }
    }

    boolean renameWalletConfig(WalletConfig walletConfig, @NonNull String newAlias) {
        if (doesWalletConfigExist(walletConfig)) {
            WalletConfig tempConfig = getConnectionById(walletConfig.getId());
            tempConfig.setAlias(newAlias);
            mConnections.remove(walletConfig);
            mConnections.add(tempConfig);
            return true;
        } else {
            return false;
        }
    }
}
