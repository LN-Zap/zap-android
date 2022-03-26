package zapsolutions.zap.connection.manageNodeConfigs;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class NodeConfigsJson {

    @SerializedName("connections")
    Set<NodeConfig> mConnections;

    @SerializedName("version")
    int version;

    public NodeConfig getConnectionById(@NonNull String id) {
        for (NodeConfig nodeConnectionConfig : mConnections) {
            if (nodeConnectionConfig.getId().equals(id)) {
                return nodeConnectionConfig;
            }
        }
        return null;
    }

    public NodeConfig getConnectionByAlias(@NonNull String alias) {
        for (NodeConfig nodeConnectionConfig : mConnections) {
            if (nodeConnectionConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                return nodeConnectionConfig;
            }
        }
        return null;
    }

    public Set<NodeConfig> getConnections() {
        return mConnections;
    }

    boolean doesNodeConfigExist(@NonNull NodeConfig nodeConfig) {
        return mConnections.contains(nodeConfig);
    }

    boolean addNode(@NonNull NodeConfig nodeConfig) {
        return mConnections.add(nodeConfig);
    }

    boolean removeNodeConfig(NodeConfig nodeConfig) {
        return mConnections.remove(nodeConfig);
    }

    boolean updateNodeConfig(NodeConfig nodeConfig) {
        if (doesNodeConfigExist(nodeConfig)) {
            mConnections.remove(nodeConfig);
            mConnections.add(nodeConfig);
            return true;
        } else {
            return false;
        }
    }

    boolean renameNodeConfig(NodeConfig nodeConfig, @NonNull String newAlias) {
        if (doesNodeConfigExist(nodeConfig)) {
            NodeConfig tempConfig = getConnectionById(nodeConfig.getId());
            tempConfig.setAlias(newAlias);
            mConnections.remove(nodeConfig);
            mConnections.add(tempConfig);
            return true;
        } else {
            return false;
        }
    }
}
