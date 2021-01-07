package zapsolutions.zap.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;

public class KeystoreUtil {
    private static final String KEY_PIN_ACTIVE = "PinActiveKey";
    private static final String ANDROID_KEY_STORE_NAME = "AndroidKeyStore";
    private final static Object s_keyInitLock = new Object();

    public void addPinActiveKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_PIN_ACTIVE)) {
            generateKey(KEY_PIN_ACTIVE);
        }
    }

    public void removePinActiveKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        synchronized (s_keyInitLock) {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
            keyStore.load(null);
            keyStore.deleteEntry(KEY_PIN_ACTIVE);
        }
    }

    public boolean isPinActive() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
        keyStore.load(null);
        return keyStore.containsAlias(KEY_PIN_ACTIVE);
    }

    protected void generateKey(String keyAlias) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        synchronized (s_keyInitLock) {
            KeyGenerator keyGenerator;
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE_NAME);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(keyAlias,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build());
            keyGenerator.generateKey();
        }
    }
}
