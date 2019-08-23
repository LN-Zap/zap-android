package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.util.PrefsUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

/**
 * This SINGLETON class is used to load and save configurations for wallets.
 * Multiple wallets can exist simultaneously, but each alias (wallet name) is only allowed to exist once.
 * <p>
 * The wallet configurations are stored encrypted in the default shared preferences.
 */
public class WalletConfigsManager {

    public static final String DEFAULT_WALLET_NAME = "Default Wallet";
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
            createEmptyWalletConfigsJson();
        }

        if (mWalletConfigsJson == null) {
            createEmptyWalletConfigsJson();
        }
    }

    // used for unit tests
    public WalletConfigsManager(String walletConfigsJson) {
        try {
            mWalletConfigsJson = new Gson().fromJson(walletConfigsJson, WalletConfigsJson.class);
        } catch (JsonSyntaxException e) {
            createEmptyWalletConfigsJson();
        }
        if (mWalletConfigsJson == null) {
            createEmptyWalletConfigsJson();
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

    private void createEmptyWalletConfigsJson() {
        mWalletConfigsJson = new Gson().fromJson("{\"connections\":[]}", WalletConfigsJson.class);
    }

    /**
     * Checks if a wallet configuration with the given alias/name already exists.
     *
     * @param alias The name of the wallet
     * @return
     */
    public boolean doesWalletConfigExist(@NonNull String alias) {
        return mWalletConfigsJson.doesWalletConfigExist(alias);
    }


    /**
     * Adds a wallet configuration to our current setup.
     * If a configuration exist with this alias, it will be overridden.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param alias    Name of the wallet/configuration
     * @param type     One of the following types: remote, local
     * @param host     The host
     * @param port     The port
     * @param cert     The certificate. This is optional and can be null
     * @param macaroon The Macaroon. Encoded as base16 (hex)
     */
    public void addWalletConfig(@NonNull String alias, String type, String host,
                                int port, @Nullable String cert, String macaroon) {

        // Create the config
        WalletConfig config = new WalletConfig();
        config.setAlias(alias);
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setCert(cert);
        config.setMacaroon(macaroon);


        // Add the config to our configurations array
        mWalletConfigsJson.addWalletConfig(config);

    }


    /**
     * Returns the wallet config of the currently active wallet.
     *
     * @return
     */
    public WalletConfig getCurrentWalletConfig() {
        WalletConfig config = getWalletConfig(PrefsUtil.getCurrentWalletConfig());
        if (config == null && mWalletConfigsJson.mConnections.size() > 0) {
            PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mWalletConfigsJson.mConnections.get(0).getAlias()).commit();
            return mWalletConfigsJson.mConnections.get(0);
        }

        return config;
    }


    /**
     * Load a wallet configuration by its alias/name.
     *
     * @param alias The name of the wallet
     * @return Returns null if no configuration is found for the given alias
     */
    public WalletConfig getWalletConfig(@NonNull String alias) {
        return mWalletConfigsJson.getConnection(alias);
    }


    /**
     * Renames the desired wallet config.
     *
     * @param oldAlias
     * @param newAlias
     * @return false if the old alias did not exist.
     */
    public boolean renameWalletConfig(String oldAlias, @NonNull String newAlias) {
        return mWalletConfigsJson.renameConnection(oldAlias, newAlias);
    }

    /**
     * Removes the desired wallet config.
     *
     * @param alias
     */
    public boolean removeWalletConfig(@NonNull String alias) {
        return mWalletConfigsJson.removeConnection(alias);
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
