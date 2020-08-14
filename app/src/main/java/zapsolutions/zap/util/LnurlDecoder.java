package zapsolutions.zap.util;

public class LnurlDecoder {

    public static String decode(String lnurl) throws NoLnUrlDataException {

        if (lnurl == null) {
            throw new NoLnUrlDataException("LNURL decoding failed: The data to decode is not a LNURL");
        }

        // Remove the "lightning:" uri scheme if it is present
        lnurl = UriUtil.removeURI(lnurl);

        if (lnurl.length() < 5 || !lnurl.substring(0, 5).toLowerCase().equals("lnurl")) {
            throw new NoLnUrlDataException("LNURL decoding failed: The data to decode is not a LNURL");
        }

        String decodedLnurl = null;
        try {
            byte[] decodedBech32 = Bech32.bech32Decode(lnurl, false).second;
            byte[] regroupedBytes = Bech32.regroupBytes(decodedBech32);

            decodedLnurl = new String(regroupedBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("LNURL decoding failed: " + e.getMessage());
        }
        return decodedLnurl;
    }

    public static class NoLnUrlDataException extends Exception {
        public NoLnUrlDataException(String errorMessage) {
            super(errorMessage);
        }
    }
}
