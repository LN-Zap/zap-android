package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletConfigsJson {

    @SerializedName("connections")
    List<WalletConfig> mConnections;

    public WalletConfig getConnection(@NonNull String alias) {
        for (WalletConfig walletConnectionConfig : mConnections) {
            if (walletConnectionConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                return walletConnectionConfig;
            }
        }

        return null;
    }

    boolean doesWalletConfigExist(String alias) {
        return getConnection(alias) != null;
    }

    void addWalletConfig(@NonNull WalletConfig walletConfig) {

        // Test if it already exist
        if (doesWalletConfigExist(walletConfig.getAlias())) {
            int tempIndex = getWalletIndex(walletConfig.getAlias().toLowerCase());
            // It exists, replace it.
            mConnections.set(tempIndex, walletConfig);
        } else {
            // Nothing exist yet, create a new one.
            mConnections.add(walletConfig);
        }

    }


    public boolean removeConnection(String alias) {

        if (doesWalletConfigExist(alias)) {
            int tempIndex = getWalletIndex(alias);
            mConnections.remove(tempIndex);
            return true;
        }
        return false;
    }

    public boolean renameConnection(String oldAlias, String newAlias) {

        if (doesWalletConfigExist(oldAlias)) {
            int tempIndex = getWalletIndex(oldAlias);
            mConnections.get(tempIndex).setAlias(newAlias.toLowerCase());
            return true;
        }
        return false;
    }

    private int getWalletIndex(@NonNull String alias) {
        int tempIndex = -1;
        for (WalletConfig tempConfig : mConnections) {
            if (tempConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                tempIndex = mConnections.indexOf(tempConfig);
                break;
            }
        }
        return tempIndex;
    }
}
