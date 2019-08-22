package zapsolutions.zap.util;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class UtilFunctions {
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final String LOG_TAG = UtilFunctions.class.getName();

    public static String sha256Hash(String data) {
        // TODO: Add keyStretching function to secure against brute force
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String sha256HashZapSalt(String data) {
        return sha256Hash(data + getZapsalt());
    }

    public static String pinHash(String data) {
        //HmacSHA1 with PBKDF2 and ZapSalt
        byte[] hash = new byte[0];
        try {
            hash = encodePbkdf2(data.toCharArray(), getZapsalt().getBytes(), RefConstants.NUM_HASH_ITERATIONS, 32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return bytesToHex(hash);
    }

    public static String getZapsalt() {

        if (!PrefsUtil.getPrefs().contains(PrefsUtil.RANDOM_SOURCE)) {
            createRandomSource();
        }
        String salt = "";
        try {
            String decrypted = new Cryptography(App.getAppContext()).decryptData(PrefsUtil.getPrefs().getString(PrefsUtil.RANDOM_SOURCE, ""));
            salt = "zap" + decrypted;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return salt;
    }

    public static void createRandomSource() {
        try {
            SecureRandom random = new SecureRandom();
            int randomNumber = random.nextInt();
            String encrypted = new Cryptography(App.getAppContext()).encryptData(String.valueOf(randomNumber));
            PrefsUtil.edit().putString(PrefsUtil.RANDOM_SOURCE, encrypted).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] encodePbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
    }
}
