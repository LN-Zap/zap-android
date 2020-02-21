package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.util.PrefsUtil;
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

        String encrypted = PrefsUtil.getPrefs().getString(PrefsUtil.WALLET_CONFIGS, "");

        String decrypted = null;
        try {
            decrypted = new Cryptography(App.getAppContext()).decryptData(encrypted);
        } catch (Exception e) {
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
    public static boolean isValidJson(String walletConfigsString) {
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
        return new Gson().fromJson("{\"connections\":[]}", WalletConfigsJson.class);
    }

    /**
     * Checks if a wallet configuration with the given UUID exists.
     *
     * @param id The UUID of the wallet
     * @return
     */
    public boolean doesWalletConfigExist(@NonNull String id) {
        return mWalletConfigsJson.doesWalletConfigExist(id);
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
    public String addWalletConfig(@NonNull String alias, @NonNull String type, String host,
                                  int port, @Nullable String cert, String macaroon) {

        // Create the UUID for the new config
        String id = UUID.randomUUID().toString();

        // Create the config
        WalletConfig config = new WalletConfig();
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);
        config.setId(id);

        // Add the config to our configurations array
        mWalletConfigsJson.addWallet(config);

        ZapLog.debug(LOG_TAG, "The wallet ID is:" + id);

        return id;
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
    public String updateWalletConfig(@NonNull String id, @NonNull String alias, @NonNull String type, String host,
                                     int port, @Nullable String cert, String macaroon) {

        // Create the config
        WalletConfig config = new WalletConfig();
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);
        config.setId(id);

        // Add the config to our configurations array
        mWalletConfigsJson.addWallet(config);

        return id;
    }


    /**
     * Returns the wallet config of the currently active wallet.
     *
     * @return
     */
    public WalletConfig getCurrentWalletConfig() {
        WalletConfig config = getWalletConfig(PrefsUtil.getCurrentWalletConfig());
        if (config == null && mWalletConfigsJson.mConnections.size() > 0) {
            PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mWalletConfigsJson.mConnections.get(0).getId()).commit();
            return mWalletConfigsJson.mConnections.get(0);
        }

        return config;
    }


    /**
     * Load a wallet configuration by its UUID.
     *
     * @param id The UUID of the wallet
     * @return Returns null if no configuration is found for the given uuid
     */
    public WalletConfig getWalletConfig(@NonNull String id) {
        return mWalletConfigsJson.getConnection(id);
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
     *
     * @param id       The UUID of the wallet that should be renamed.
     * @param newAlias The new alias
     * @return false if the old alias did not exist.
     */
    public boolean renameWalletConfig(String id, @NonNull String newAlias) {
        return mWalletConfigsJson.renameWalletConfig(id, newAlias);
    }

    /**
     * Removes the desired wallet config.
     *
     * @param id
     */
    public boolean removeWalletConfig(@NonNull String id) {
        return mWalletConfigsJson.removeWalletConfig(id);
    }

    public boolean hasAtLeastOneConfig() {
        return mWalletConfigsJson.getConnections().size() != 0;
    }

    public boolean hasLocalConfig() {
        if (!hasAtLeastOneConfig()) {
            return false;
        } else {
            boolean hasLocal = false;
            for (int i = 0; i < mWalletConfigsJson.getConnections().size(); i++) {
                if (mWalletConfigsJson.getConnections().get(i).isLocal()) {
                    hasLocal = true;
                    break;
                }
            }
            return hasLocal;
        }
    }

    public void removeAllWalletConfigs(){
        mWalletConfigsJson = createEmptyWalletConfigsJson();
    }

    /**
     * Saves the current state of wallet configs encrypted to default shared preferences.
     * Always use this after you have changed anything on the configurations.
     *
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnrecoverableEntryException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws NoSuchProviderException
     * @throws BadPaddingException
     * @throws KeyStoreException
     * @throws IllegalBlockSizeException
     */
    public void apply() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, KeyStoreException, IllegalBlockSizeException {
        // Convert JSON object to string
        String jsonString = new Gson().toJson(mWalletConfigsJson);

        // Save the new WalletConfigurations in encrypted prefs
        String encrypted = new Cryptography(App.getAppContext()).encryptData(jsonString);
        PrefsUtil.edit().putString(PrefsUtil.WALLET_CONFIGS, encrypted).commit();
    }
}
