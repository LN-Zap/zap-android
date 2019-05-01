package ln_zap.zap;

import java.security.MessageDigest;

public class UtilFunctions {
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
        // TODO: Do salt per device to prevent rainbow tables on stolen hashes
        return sha256Hash(data + "zapsalt");
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

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
