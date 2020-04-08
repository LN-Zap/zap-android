package zapsolutions.zap.lnurl;

import zapsolutions.zap.util.ZapLog;

public class Lnurl {

    private static final String LOG_TAG = Lnurl.class.getName();

    public static String decode(String lnurl) {
        String decodedLnurl = null;
        try {
            byte[] decodedBech32 = Bech32.bech32Decode(lnurl, false).getRight();

            // Translate the bytes to 5 bit groups, most significant bit first.
            boolean[] bitArray = new boolean[decodedBech32.length * 5];
            for (int i = 0; i < decodedBech32.length; i++) {
                for (int j = 3; j < 8; j++)
                    bitArray[i * 5 + j - 3] = (decodedBech32[i] & (byte) (128 / Math.pow(2, j))) != 0;
            }

            // Re-arrange those bits into groups of 8 bits.
            byte[] regroupedBits = booleanArrayToBytes(bitArray);

            decodedLnurl = new String(regroupedBits);
            ZapLog.debug(LOG_TAG, "Decoded LNURL: " + decodedLnurl);
        } catch (Exception e) {
            e.printStackTrace();
            ZapLog.debug(LOG_TAG, "LNURL DECODE ERROR: " + e.getMessage());
        }
        return decodedLnurl;
    }

    private static byte[] booleanArrayToBytes(boolean[] input) {
        byte[] toReturn = new byte[input.length / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (input[entry * 8 + bit]) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }

        return toReturn;
    }
}
