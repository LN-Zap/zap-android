package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletConfigsJson {

    @SerializedName("connections")
    List<WalletConfig> mConnections;

    public WalletConfig getConnection(@NonNull String id) {
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

    public List<WalletConfig> getConnections() {
        return mConnections;
    }

    boolean doesWalletConfigExist(String id) {
        return getConnection(id) != null;
    }

    void addWallet(@NonNull WalletConfig walletConfig) {

        // Test if it already exist
        if (doesWalletConfigExist(walletConfig.getId())) {
            int tempIndex = getWalletConfigIndex(walletConfig.getId());
            // It exists, replace it.
            mConnections.set(tempIndex, walletConfig);
        } else {
            // Nothing exist yet, create a new one.
            mConnections.add(walletConfig);
        }

    }


    public boolean removeWalletConfig(String id) {

        if (doesWalletConfigExist(id)) {
            int tempIndex = getWalletConfigIndex(id);
            mConnections.remove(tempIndex);
            return true;
        }
        return false;
    }

    public boolean renameWalletConfig(String id, String newAlias) {
        if (doesWalletConfigExist(id)) {
            int tempIndex = getWalletConfigIndex(id);
            mConnections.get(tempIndex).setAlias(newAlias);
            return true;
        }
        return false;
    }

    private int getWalletConfigIndex(@NonNull String id) {
        int tempIndex = -1;
        for (WalletConfig tempConfig : mConnections) {
            if (tempConfig.getId().equals(id)) {
                tempIndex = mConnections.indexOf(tempConfig);
                break;
            }
        }
        return tempIndex;
    }
}
