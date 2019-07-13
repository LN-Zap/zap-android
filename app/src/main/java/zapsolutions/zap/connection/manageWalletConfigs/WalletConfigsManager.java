package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.ZapLog;

/**
 * This class is used to read and save the configurations for wallets.
 * Multiple wallets can exist simultaneously, but each alias (wallet name) is only allowed to exist once.
 * <p>
 * The wallet configurations are stored encrypted in the default shared preferences.
 */
public class WalletConfigsManager {

    private static final String LOG_TAG = "WalletConfigsManager";

    private String mWalletConfigsJsonString;

    public WalletConfigsManager() {
        String encryptedWalletConfigs = PrefsUtil.getPrefs().getString(PrefsUtil.WALLET_CONFIGS, null);

        //ZapLog.debug(LOG_TAG, encryptedConnectionConfigs);

        // ToDo: Decrypt encrypted configs

        mWalletConfigsJsonString = encryptedWalletConfigs;
    }


    public WalletConfigsManager(String walletConfigsJson) {
        mWalletConfigsJsonString = walletConfigsJson;
    }


    /**
     * Used to determine if the provided String is a valid walletConfigs JSON.
     *
     * @param walletConfigsString parses as JSON
     * @return if the JSON syntax is valid
     */
    public static boolean isValidJson(String walletConfigsString) {
        try {
            WalletConfigsJson walletConfigs = new Gson().fromJson(walletConfigsString, WalletConfigsJson.class);
            return walletConfigs != null;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }


    /**
     * Checks if a wallet configuration under this alias/name already exists.
     *
     * @param alias The name of the wallet
     * @return
     */
    public boolean doesWalletConfigExist(String alias) {
        if (alias == null) {
            return false;
        }
        WalletConfigsJson walletConfigs = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        return walletConfigs.getConnection(alias) != null;
    }


    /**
     * Saves a wallet configuration encrypted in the default shared preferences.
     * If an configuration exist with this alias, it will be overridden.
     *
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, onDevice
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public void saveWalletConfig(String alias, String type, String host,
                                 int port, @Nullable String cert, String macaroon) {

        WalletConfigsJson walletConfigs;

        if (mWalletConfigsJsonString == null) {
            // No wallet connection configurations exists yet. Create the json array that will contain our first connection configuration.
            walletConfigs = new Gson().fromJson("{\"connections\":[]}", WalletConfigsJson.class);
        } else {
            // Get current wallet connection configs
            walletConfigs = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        }

        //ZapLog.debug(LOG_TAG, mWalletConfigsJsonString);


        // Create the config
        WalletConfig config = new WalletConfig();
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);


        // Add the config to our configurations array

        // Test if it already exist
        if (doesWalletConfigExist(alias)) {
            int tempIndex = -1;
            for (WalletConfig tempConfig : walletConfigs.mConnections) {
                if (tempConfig.getAlias().equals(alias)) {
                    tempIndex = walletConfigs.mConnections.indexOf(tempConfig);
                    break;
                }
            }
            walletConfigs.mConnections.set(tempIndex, config);
        } else {
            // Nothing exist yet, create a new one.
            walletConfigs.mConnections.add(config);
        }

        String jsonString = new Gson().toJson(walletConfigs);

        // ToDo: Encrypt the connection information.

        // Save the new WalletConfigurations in the default prefs
        PrefsUtil.edit().putString(PrefsUtil.WALLET_CONFIGS, jsonString).apply();

        ZapLog.debug(LOG_TAG, jsonString);
    }

    /**
     * Returns the wallet config of the currently active wallet.
     * @return
     */
    public WalletConfig loadCurrentWalletConfig() {
        return loadWalletConfig(PrefsUtil.getCurrentWalletConfig());
    }


    /**
     * Load a wallet configuration by its alias/name.
     *
     * @param alias The name of the wallet
     * @return Returns null if no configuration is found for the given alias
     */
    public WalletConfig loadWalletConfig(String alias) {

        WalletConfigsJson walletConfigsJson;

        try {
            walletConfigsJson = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        } catch (JsonSyntaxException ex) {
            return null;
        }

        if (!doesWalletConfigExist(alias)) {
            return null;
        }

        WalletConfig config = new WalletConfig();

        config.setAlias(walletConfigsJson.getConnection(alias).getAlias().toLowerCase());
        config.setHost(walletConfigsJson.getConnection(alias).getHost());
        config.setPort(walletConfigsJson.getConnection(alias).getPort());
        config.setCert(walletConfigsJson.getConnection(alias).getCert());
        config.setMacaroon(walletConfigsJson.getConnection(alias).getMacaroon());

        return config;
    }


    /**
     * Removes the desired wallet config.
     *
     * @param alias
     */
    public void deleteWalletConfig(String alias) {
        WalletConfigsJson walletConfigs;

        if (mWalletConfigsJsonString == null) {
            return;
        } else {
            // Get current wallet connection configs
            walletConfigs = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        }

        if (doesWalletConfigExist(alias)) {
            int tempIndex = -1;
            for (WalletConfig tempConfig : walletConfigs.mConnections) {
                if (tempConfig.getAlias().equals(alias)) {
                    tempIndex = walletConfigs.mConnections.indexOf(tempConfig);
                    break;
                }
            }
            walletConfigs.mConnections.remove(tempIndex);
        }
    }

}
