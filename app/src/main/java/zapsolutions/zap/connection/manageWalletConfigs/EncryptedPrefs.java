package zapsolutions.zap.connection.manageWalletConfigs;

import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.UtilFunctions;
import zapsolutions.zap.util.ZapLog;


public class EncryptedPrefs {

    private static final String LOG_TAG = EncryptedPrefs.class.getName();

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "ZapKey";

    private KeyStore mKeyStore;

    public EncryptedPrefs() {
        try {
            initKeyStore();
        } catch (KeyStoreException e) {
            ZapLog.debug(LOG_TAG, "KeyStoreException");
            e.printStackTrace();
        } catch (CertificateException e) {
            ZapLog.debug(LOG_TAG, "CertificateException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            ZapLog.debug(LOG_TAG, "NoSuchAlgorithm");
            e.printStackTrace();
        } catch (IOException e) {
            ZapLog.debug(LOG_TAG, "IO Exception");
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            ZapLog.debug(LOG_TAG, "NoSuchProvider");
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            ZapLog.debug(LOG_TAG, "InvalidAlgorithmParameterException");
            e.printStackTrace();
        }
    }

    /**
     * Read a string from the encrypted preferences.
     *
     * @param prefName     The name of the string you are requesting.
     * @param defaultValue If nothing is there, this will be returned.
     * @return
     */
    public String getString(String prefName, String defaultValue) {
        App ctx = App.getAppContext();

        SharedPreferences encryptedPrefs = Armadillo.create(ctx, PrefsUtil.PREFS_ENCRYPTED)
                .encryptionFingerprint(ctx)
                .keyStretchingFunction(new PBKDF2KeyStretcher(RefConstants.NUM_HASH_ITERATIONS, null))
                .password("test".toCharArray())
                .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                .build();

        return encryptedPrefs.getString(prefName, defaultValue);
    }

    /**
     * Save a string encrypted in the encrypted preferences.
     *
     * @param data     The string to encrypt
     * @param prefName The name which has to be used later to request the data
     */
    public void putString(String data, String prefName) {


        App ctx = App.getAppContext();
        SharedPreferences encryptedPrefs = Armadillo.create(ctx, PrefsUtil.PREFS_ENCRYPTED)
                .encryptionFingerprint(ctx)
                .keyStretchingFunction(new PBKDF2KeyStretcher(RefConstants.NUM_HASH_ITERATIONS, null))
                .password("test".toCharArray())
                .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                .build();

        // We use commit here, as we want to be sure, that the data is saved and readable when we want to access it in the next step.
        encryptedPrefs.edit()
                .putString(prefName, data)
                .commit();
    }

    /**
     * Creates a RSA key pair, if none does exist yet
     *
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     */
    private void initKeyStore() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
        mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        mKeyStore.load(null);

        // Generate the RSA key pairs
        if (!mKeyStore.containsAlias(KEY_ALIAS)) {
            ZapLog.debug(LOG_TAG, "KEY Pair does not exist.");
            // Generate a key pair for encryption
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(App.getAppContext())
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        } else {
            ZapLog.debug(LOG_TAG, "KEY Pair exist.");
        }
    }

    /**
     * Fetches the private key from a KeyStore entry and converts it to char[]
     *
     * @param alias
     * @return
     */
    private char[] getPrivateKey(final String alias) {

        byte[] keyByte = null;
        String keyString = "";
        char[] key = null;

        try {

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(alias, null);

            keyByte = privateKeyEntry.getPrivateKey().getEncoded();

            try {
                keyString = new String(keyByte, "UTF-8");   // if the charset is UTF-8
                key = keyString.toCharArray();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            ZapLog.debug(LOG_TAG, "NoSuchAlgorithm");
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            ZapLog.debug(LOG_TAG, "UnrecoverableEntry");
            e.printStackTrace();
        } catch (KeyStoreException e) {
            ZapLog.debug(LOG_TAG, "KeyStoreException");
            e.printStackTrace();
        }
        ZapLog.debug(LOG_TAG, keyString);
        return key;
    }
}
