package zapsolutions.zap.util;

import android.os.Build;
import android.provider.Settings;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import zapsolutions.zap.baseClasses.App;

import java.security.MessageDigest;

public class UtilFunctions {
    private final static char[] hexArray = "0123456789abcdef".toCharArray();

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
        PBKDF2KeyStretcher keyStretcher = new PBKDF2KeyStretcher(5000, null);
        return bytesToHex(keyStretcher.stretch((getZapsalt() + "pin").getBytes(), data.toCharArray(), 32));
    }

    public static String getZapsalt() {
        String androidID = Settings.Secure.getString(App.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String salt = "zap" + Build.MANUFACTURER + Build.BRAND + Build.MODEL + Build.SERIAL + androidID;

        return salt;
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
}
