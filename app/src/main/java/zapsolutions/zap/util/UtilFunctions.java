package zapsolutions.zap.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UtilFunctions {
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final String LOG_TAG = UtilFunctions.class.getName();

    public static String sha256Hash(String data) {
        // TODO: Add keyStretching function to secure against brute force
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Couldn't encode pin with PBKDF2", e);
        }

        return bytesToHex(hash);
    }

    public static String getZapsalt() {

        try {
            if (!PrefsUtil.getEncryptedPrefs().contains(PrefsUtil.RANDOM_SOURCE)) {
                createRandomSource();
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String salt = "";
        try {
            String decrypted = PrefsUtil.getEncryptedPrefs().getString(PrefsUtil.RANDOM_SOURCE, "");
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
            PrefsUtil.editEncryptedPrefs().putString(PrefsUtil.RANDOM_SOURCE, String.valueOf(randomNumber)).commit();
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

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static byte[] encodePbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
    }

    public static String getQueryParam(URL url, String parameter) {
        if (url != null && url.getQuery() != null) {
            String[] params = url.getQuery().split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                if (parameter.equals(name)) {
                    return param.split("=")[1];
                }
            }
        }
        return null;
    }

    public static boolean isHex(String input) {
        return input.matches("^[0-9a-fA-F]+$");
    }
}