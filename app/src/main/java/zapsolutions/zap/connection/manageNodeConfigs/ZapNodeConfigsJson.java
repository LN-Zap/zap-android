package zapsolutions.zap.connection.manageNodeConfigs;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class ZapNodeConfigsJson {

    @SerializedName("connections")
    Set<ZapNodeConfig> mConnections;

    @SerializedName("version")
    int version;

    public ZapNodeConfig getConnectionById(@NonNull String id) {
        for (ZapNodeConfig nodeConnectionConfig : mConnections) {
            if (nodeConnectionConfig.getId().equals(id)) {
                return nodeConnectionConfig;
            }
        }
        return null;
    }

    public ZapNodeConfig getConnectionByAlias(@NonNull String alias) {
        for (ZapNodeConfig nodeConnectionConfig : mConnections) {
            if (nodeConnectionConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                return nodeConnectionConfig;
            }
        }
        return null;
    }

    public Set<ZapNodeConfig> getConnections() {
        return mConnections;
    }

    boolean doesNodeConfigExist(@NonNull ZapNodeConfig zapNodeConfig) {
        return mConnections.contains(zapNodeConfig);
    }

    boolean addNode(@NonNull ZapNodeConfig zapNodeConfig) {
        return mConnections.add(zapNodeConfig);
    }

    boolean removeNodeConfig(ZapNodeConfig zapNodeConfig) {
        return mConnections.remove(zapNodeConfig);
    }

    boolean updateNodeConfig(ZapNodeConfig zapNodeConfig) {
        if (doesNodeConfigExist(zapNodeConfig)) {
            mConnections.remove(zapNodeConfig);
            mConnections.add(zapNodeConfig);
            return true;
        } else {
            return false;
        }
    }

    boolean renameNodeConfig(ZapNodeConfig zapNodeConfig, @NonNull String newAlias) {
        if (doesNodeConfigExist(zapNodeConfig)) {
            ZapNodeConfig tempConfig = getConnectionById(zapNodeConfig.getId());
            tempConfig.setAlias(newAlias);
            mConnections.remove(zapNodeConfig);
            mConnections.add(tempConfig);
            return true;
        } else {
            return false;
        }
    }
}
