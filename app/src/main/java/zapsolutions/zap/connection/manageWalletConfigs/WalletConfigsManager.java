package zapsolutions.zap.connection.manageWalletConfigs;

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
 * This SINGLETON class is used to load and save configurations for wallets.
 * Multiple wallets can exist simultaneously.
 * <p>
 * The wallet configurations are stored encrypted in the default shared preferences.
 */
public class WalletConfigsManager {

    public static final String DEFAULT_LOCAL_WALLET_NAME = "Zap-Android";
    private static final String LOG_TAG = WalletConfigsManager.class.getName();
    private static WalletConfigsManager mInstance;
    private WalletConfigsJson mWalletConfigsJson;

    private WalletConfigsManager() {

        String decrypted = null;
        try {
            decrypted = PrefsUtil.getEncryptedPrefs().getString(PrefsUtil.WALLET_CONFIGS, "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        if (isValidJson(decrypted)) {
            mWalletConfigsJson = new Gson().fromJson(decrypted, WalletConfigsJson.class);
        } else {
            mWalletConfigsJson = createEmptyWalletConfigsJson();
        }

        if (mWalletConfigsJson == null) {
            mWalletConfigsJson = createEmptyWalletConfigsJson();
        }
    }

    // used for unit tests
    public WalletConfigsManager(String walletConfigsJson) {
        try {
            mWalletConfigsJson = new Gson().fromJson(walletConfigsJson, WalletConfigsJson.class);
        } catch (JsonSyntaxException e) {
            mWalletConfigsJson = createEmptyWalletConfigsJson();
        }
        if (mWalletConfigsJson == null) {
            mWalletConfigsJson = createEmptyWalletConfigsJson();
        }
    }

    public static WalletConfigsManager getInstance() {
        if (mInstance == null) {
            mInstance = new WalletConfigsManager();
        }
        return mInstance;
    }

    /**
     * Used to determine if the provided String is a valid walletConfigs JSON.
     *
     * @param walletConfigsString parses as JSON
     * @return if the JSON syntax is valid
     */
    private static boolean isValidJson(String walletConfigsString) {
        try {
            WalletConfigsJson walletConfigs = new Gson().fromJson(walletConfigsString, WalletConfigsJson.class);
            return walletConfigs != null;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    public WalletConfigsJson getWalletConfigsJson() {
        return mWalletConfigsJson;
    }

    private WalletConfigsJson createEmptyWalletConfigsJson() {
        return new Gson().fromJson("{\"connections\":[], \"version\":" + RefConstants.WALLET_CONFIG_JSON_VERSION + "}", WalletConfigsJson.class);
    }

    /**
     * Checks if a wallet configuration already exists.
     *
     * @param walletConfig
     * @return
     */
    public boolean doesWalletConfigExist(@NonNull WalletConfig walletConfig) {
        return mWalletConfigsJson.doesWalletConfigExist(walletConfig);
    }

    /**
     * Checks if a wallet configuration already exists that points to the same destination.
     *
     * @param host
     * @param port
     * @return
     */
    public boolean doesDestinationExist(@NonNull String host, @NonNull int port) {
        List<WalletConfig> configList = getAllWalletConfigs(false);
        for (WalletConfig tempConfig : configList) {
            if (tempConfig.getHost().equals(host) && tempConfig.getPort() == port) {
                return true;
            }
        }
        return false;
    }


    /**
     * Adds a wallet configuration to our current setup.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public WalletConfig addWalletConfig(@NonNull String alias, @NonNull String type, String host,
                                        int port, @Nullable String cert, String macaroon) {

        // Create the UUID for the new config
        String id = UUID.randomUUID().toString();

        // Create the config
        WalletConfig config = new WalletConfig(id);
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);

        // Add the config to our configurations array
        boolean walletAdded = mWalletConfigsJson.addWallet(config);

        if (walletAdded) {
            ZapLog.d(LOG_TAG, "The ID of the created WalletConfig is:" + id);
            return config;
        } else {
            return null;
        }
    }

    /**
     * Updates a wallet configuration in our current setup.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param id       UUID of the wallet/configuration that will be updated
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public WalletConfig updateWalletConfig(@NonNull String id, @NonNull String alias, @NonNull String type, String host,
                                           int port, @Nullable String cert, String macaroon) {

        // Create the config
        WalletConfig config = new WalletConfig(id);
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);

        // Update the config in our configurations array
        boolean walletUpdated = mWalletConfigsJson.updateWalletConfig(config);

        if (walletUpdated) {
            ZapLog.d(LOG_TAG, "WalletConfig updated! (id =" + id + ")");
            return config;
        } else {
            return null;
        }
    }


    /**
     * Returns the wallet config of the currently active wallet.
     *
     * @return
     */
    public WalletConfig getCurrentWalletConfig() {
        WalletConfig config = getWalletConfigById(PrefsUtil.getCurrentWalletConfig());
        if (config == null && hasAnyConfigs()) {
            PrefsUtil.editPrefs().putString(PrefsUtil.CURRENT_WALLET_CONFIG, ((WalletConfig) mWalletConfigsJson.mConnections.toArray()[0]).getId()).commit();
            return (WalletConfig) mWalletConfigsJson.mConnections.toArray()[0];
        }
        return config;
    }


    /**
     * Load a wallet configuration by its UUID.
     *
     * @param id The UUID of the wallet
     * @return Returns null if no configuration is found for the given uuid
     */
    public WalletConfig getWalletConfigById(@NonNull String id) {
        return mWalletConfigsJson.getConnectionById(id);
    }

    /**
     * Returns a List of all wallet configs sorted alphabetically.
     *
     * @param activeOnTop if true the currently active wallet is on top, ignoring alphabetical order.
     * @return
     */
    public List<WalletConfig> getAllWalletConfigs(boolean activeOnTop) {
        List<WalletConfig> sortedList = new ArrayList<>();
        sortedList.addAll(mWalletConfigsJson.getConnections());

        if (sortedList.size() > 1) {
            // Sort the list alphabetically
            Collections.sort(sortedList);

            // Move the current config to top
            if (activeOnTop) {
                int index = -1;
                for (WalletConfig tempConfig : sortedList) {
                    if (tempConfig.getId().equals(PrefsUtil.getCurrentWalletConfig())) {
                        index = sortedList.indexOf(tempConfig);
                        break;
                    }
                }
                WalletConfig currentConfig = sortedList.get(index);
                sortedList.remove(index);
                sortedList.add(0, currentConfig);
            }
        }
        return sortedList;
    }


    /**
     * Renames the desired wallet config.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param walletConfig The wallet config that should be renamed.
     * @param newAlias     The new alias
     * @return false if the old alias did not exist.
     */
    public boolean renameWalletConfig(@NonNull WalletConfig walletConfig, @NonNull String newAlias) {
        return mWalletConfigsJson.renameWalletConfig(walletConfig, newAlias);
    }

    /**
     * Removes the desired wallet config.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param walletConfig
     */
    public boolean removeWalletConfig(@NonNull WalletConfig walletConfig) {
        return mWalletConfigsJson.removeWalletConfig(walletConfig);
    }

    public boolean hasLocalConfig() {
        if (hasAnyConfigs()) {
            boolean hasLocal = false;
            for (WalletConfig walletConfig : mWalletConfigsJson.getConnections()) {
                if (walletConfig.isLocal()) {
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
        return !mWalletConfigsJson.getConnections().isEmpty();
    }

    /**
     * Removes all wallet configs.
     * Do not forget to call apply() afterwards to make this change permanent.
     */
    public void removeAllWalletConfigs() {
        mWalletConfigsJson = createEmptyWalletConfigsJson();
    }

    /**
     * Saves the current state of wallet configs encrypted to default shared preferences.
     * Always use this after you have changed anything on the configurations.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void apply() throws GeneralSecurityException, IOException {
        // Convert JSON object to string
        String jsonString = new Gson().toJson(mWalletConfigsJson);

        // Save the new WalletConfigurations in encrypted prefs
        PrefsUtil.editEncryptedPrefs().putString(PrefsUtil.WALLET_CONFIGS, jsonString).commit();
    }
}
