package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.ZapLog;

/**
 * This class is used to getString and save the configurations for wallets.
 * Multiple wallets can exist simultaneously, but each alias (wallet name) is only allowed to exist once.
 * <p>
 * The wallet configurations are stored encrypted in the default shared preferences.
 */
public class WalletConfigsManager {

    private static final String LOG_TAG = WalletConfigsManager.class.getName();

    public static final String DEFAULT_WALLET_NAME = "DefaultWallet";

    private String mWalletConfigsJsonString;

    public WalletConfigsManager() {
        EncryptedPrefs encryptedPrefs = new EncryptedPrefs();
        String encryptedWalletConfigs = encryptedPrefs.getString(PrefsUtil.WALLET_CONFIGS, "");
        mWalletConfigsJsonString = encryptedWalletConfigs;
    }

    // used for unit tests
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
     * Checks if a wallet configuration with the given alias/name already exists.
     *
     * @param alias The name of the wallet
     * @return
     */
    public boolean doesWalletConfigExist(String alias) {

        if (alias == null) { return false; }

        WalletConfigsJson walletConfigs;

        try {
            walletConfigs = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        } catch (JsonSyntaxException ex) {
            return false;
        }

        if (walletConfigs == null){ return false; }

        return walletConfigs.getConnection(alias.toLowerCase()) != null;
    }


    /**
     * Saves a wallet configuration encrypted in the default shared preferences.
     * If an configuration exist with this alias, it will be overridden.
     *
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public void saveWalletConfig(String alias, String type, String host,
                                 int port, @Nullable String cert, String macaroon) {

        String jsonString = createWalletConfigJsonString(alias, type, host, port, cert, macaroon);

        // Save the new WalletConfigurations in encrypted prefs
        EncryptedPrefs encryptedPrefs = new EncryptedPrefs();
        encryptedPrefs.putString(jsonString, PrefsUtil.WALLET_CONFIGS);

        ZapLog.debug(LOG_TAG, jsonString);
    }

    /**
     * Creates a wallet configuration json-string, that contains all already existing wallet configurations plus the new one.
     * If an configuration exist with this alias, it will be overridden.
     *
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public String createWalletConfigJsonString(String alias, String type, String host,
                                 int port, @Nullable String cert, String macaroon) {

        WalletConfigsJson walletConfigs;

        if (mWalletConfigsJsonString == null) {
            // No wallet connection configurations exists yet. Create the json array that will contain our first connection configuration.
            walletConfigs = new Gson().fromJson("{\"connections\":[]}", WalletConfigsJson.class);
        } else if (mWalletConfigsJsonString.isEmpty()) {
            // No wallet connection configurations exists yet. Create the json array that will contain our first connection configuration.
            walletConfigs = new Gson().fromJson("{\"connections\":[]}", WalletConfigsJson.class);
        } else {
            // Get current wallet connection configs
            walletConfigs = new Gson().fromJson(mWalletConfigsJsonString, WalletConfigsJson.class);
        }


        // Create the config
        WalletConfig config = new WalletConfig();
        config.setAlias(alias.toLowerCase());
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
                if (tempConfig.getAlias().toLowerCase().equals(alias.toLowerCase())) {
                    tempIndex = walletConfigs.mConnections.indexOf(tempConfig);
                    break;
                }
            }
            // It exists, replace it.
            walletConfigs.mConnections.set(tempIndex, config);
        } else {
            // Nothing exist yet, create a new one.
            walletConfigs.mConnections.add(config);
        }

        // Convert JSON object to string
        String jsonString = new Gson().toJson(walletConfigs);

        return jsonString;
    }

    /**
     * Returns the wallet config of the currently active wallet.
     *
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
        config.setType(walletConfigsJson.getConnection(alias).getType());
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
