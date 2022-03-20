package zapsolutions.zap.connection.manageNodeConfigs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

/**
 * This SINGLETON class is used to load and save configurations for nodes.
 * Multiple nodes can exist simultaneously.
 * <p>
 * The node configurations are stored encrypted in the default shared preferences.
 */
public class NodeConfigsManager {

    private static final String LOG_TAG = NodeConfigsManager.class.getName();
    private static NodeConfigsManager mInstance;
    private NodeConfigsJson mNodeConfigsJson;

    private NodeConfigsManager() {

        String decrypted = null;
        try {
            decrypted = PrefsUtil.getEncryptedPrefs().getString(PrefsUtil.NODE_CONFIGS, "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        if (isValidJson(decrypted)) {
            mNodeConfigsJson = new Gson().fromJson(decrypted, NodeConfigsJson.class);
        } else {
            mNodeConfigsJson = createEmptyNodeConfigsJson();
        }

        if (mNodeConfigsJson == null) {
            mNodeConfigsJson = createEmptyNodeConfigsJson();
        }
    }

    // used for unit tests
    public NodeConfigsManager(String NodeConfigsJson) {
        try {
            mNodeConfigsJson = new Gson().fromJson(NodeConfigsJson, NodeConfigsJson.class);
        } catch (JsonSyntaxException e) {
            mNodeConfigsJson = createEmptyNodeConfigsJson();
        }
        if (mNodeConfigsJson == null) {
            mNodeConfigsJson = createEmptyNodeConfigsJson();
        }
    }

    public static NodeConfigsManager getInstance() {
        if (mInstance == null) {
            mInstance = new NodeConfigsManager();
        }
        return mInstance;
    }

    /**
     * Used to determine if the provided String is a valid nodeConfigs JSON.
     *
     * @param nodeConfigsString parses as JSON
     * @return if the JSON syntax is valid
     */
    private static boolean isValidJson(String nodeConfigsString) {
        try {
            NodeConfigsJson nodeConfigs = new Gson().fromJson(nodeConfigsString, NodeConfigsJson.class);
            return nodeConfigs != null;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    public NodeConfigsJson getNodeConfigsJson() {
        return mNodeConfigsJson;
    }

    private NodeConfigsJson createEmptyNodeConfigsJson() {
        return new Gson().fromJson("{\"connections\":[], \"version\":" + RefConstants.NODE_CONFIG_JSON_VERSION + "}", NodeConfigsJson.class);
    }

    /**
     * Checks if a node configuration already exists.
     *
     * @param nodeConfig
     * @return
     */
    public boolean doesNodeConfigExist(@NonNull NodeConfig nodeConfig) {
        return mNodeConfigsJson.doesNodeConfigExist(nodeConfig);
    }

    /**
     * Checks if a node configuration already exists that points to the same destination.
     *
     * @param host
     * @param port
     * @return
     */
    public boolean doesDestinationExist(@NonNull String host, @NonNull int port) {
        List<NodeConfig> configList = getAllNodeConfigs(false);
        for (NodeConfig tempConfig : configList) {
            if (tempConfig.getHost().equals(host) && tempConfig.getPort() == port) {
                return true;
            }
        }
        return false;
    }


    /**
     * Adds a node configuration to our current setup.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param alias    Name of the node/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public NodeConfig addNodeConfig(@NonNull String alias, @NonNull String type, String host,
                                    int port, @Nullable String cert, String macaroon) {

        // Create the UUID for the new config
        String id = UUID.randomUUID().toString();

        // Create the config
        NodeConfig config = new NodeConfig(id);
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);

        // Add the config to our configurations array
        boolean nodeAdded = mNodeConfigsJson.addNode(config);

        if (nodeAdded) {
            ZapLog.d(LOG_TAG, "The ID of the created NodeConfig is:" + id);
            return config;
        } else {
            return null;
        }
    }

    /**
     * Updates a node configuration in our current setup.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param id       UUID of the node/configuration that will be updated
     * @param alias    Name of the node/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public NodeConfig updateNodeConfig(@NonNull String id, @NonNull String alias, @NonNull String type, String host,
                                       int port, @Nullable String cert, String macaroon) {

        // Create the config
        NodeConfig config = new NodeConfig(id);
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);

        // Update the config in our configurations array
        boolean nodeUpdated = mNodeConfigsJson.updateNodeConfig(config);

        if (nodeUpdated) {
            ZapLog.d(LOG_TAG, "NodeConfig updated! (id =" + id + ")");
            return config;
        } else {
            return null;
        }
    }


    /**
     * Returns the node config of the currently active node.
     *
     * @return
     */
    public NodeConfig getCurrentNodeConfig() {
        NodeConfig config = getNodeConfigById(PrefsUtil.getCurrentNodeConfig());
        if (config == null && hasAnyConfigs()) {
            PrefsUtil.editPrefs().putString(PrefsUtil.CURRENT_NODE_CONFIG, ((NodeConfig) mNodeConfigsJson.mConnections.toArray()[0]).getId()).commit();
            return (NodeConfig) mNodeConfigsJson.mConnections.toArray()[0];
        }
        return config;
    }


    /**
     * Load a node configuration by its UUID.
     *
     * @param id The UUID of the node
     * @return Returns null if no configuration is found for the given uuid
     */
    public NodeConfig getNodeConfigById(@NonNull String id) {
        return mNodeConfigsJson.getConnectionById(id);
    }

    /**
     * Returns a List of all node configs sorted alphabetically.
     *
     * @param activeOnTop if true the currently active node is on top, ignoring alphabetical order.
     * @return
     */
    public List<NodeConfig> getAllNodeConfigs(boolean activeOnTop) {
        List<NodeConfig> sortedList = new ArrayList<>();
        sortedList.addAll(mNodeConfigsJson.getConnections());

        if (sortedList.size() > 1) {
            // Sort the list alphabetically
            Collections.sort(sortedList);

            // Move the current config to top
            if (activeOnTop) {
                int index = -1;
                for (NodeConfig tempConfig : sortedList) {
                    if (tempConfig.getId().equals(PrefsUtil.getCurrentNodeConfig())) {
                        index = sortedList.indexOf(tempConfig);
                        break;
                    }
                }
                NodeConfig currentConfig = sortedList.get(index);
                sortedList.remove(index);
                sortedList.add(0, currentConfig);
            }
        }
        return sortedList;
    }


    /**
     * Renames the desired node config.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param nodeConfig The node config that should be renamed.
     * @param newAlias   The new alias
     * @return false if the old alias did not exist.
     */
    public boolean renameNodeConfig(@NonNull NodeConfig nodeConfig, @NonNull String newAlias) {
        return mNodeConfigsJson.renameNodeConfig(nodeConfig, newAlias);
    }

    /**
     * Removes the desired node config.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param nodeConfig
     */
    public boolean removeNodeConfig(@NonNull NodeConfig nodeConfig) {
        return mNodeConfigsJson.removeNodeConfig(nodeConfig);
    }

    public boolean hasLocalConfig() {
        if (hasAnyConfigs()) {
            boolean hasLocal = false;
            for (NodeConfig nodeConfig : mNodeConfigsJson.getConnections()) {
                if (nodeConfig.isLocal()) {
                    hasLocal = true;
                    break;
                }
            }
            return hasLocal;
        } else {
            return false;
        }
    }

    public boolean hasAnyConfigs() {
        return !mNodeConfigsJson.getConnections().isEmpty();
    }

    /**
     * Removes all node configs.
     * Do not forget to call apply() afterwards to make this change permanent.
     */
    public void removeAllNodeConfigs() {
        mNodeConfigsJson = createEmptyNodeConfigsJson();
    }

    /**
     * Saves the current state of node configs encrypted to default shared preferences.
     * Always use this after you have changed anything on the configurations.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void apply() throws GeneralSecurityException, IOException {
        // Convert JSON object to string
        String jsonString = new Gson().toJson(mNodeConfigsJson);

        // Save the new node configurations in encrypted prefs
        PrefsUtil.editEncryptedPrefs().putString(PrefsUtil.NODE_CONFIGS, jsonString).commit();
    }
}
