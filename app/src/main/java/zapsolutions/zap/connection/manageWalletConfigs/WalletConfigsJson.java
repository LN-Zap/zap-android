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

}
