package zapsolutions.zap.lnurl;

import zapsolutions.zap.util.Bech32;
import zapsolutions.zap.util.UriUtil;

/**
 * This class manages the decoding of bech32 encoded lnurls.
 *
 * Please refer to the following specification:
 * https://github.com/fiatjaf/lnurl-rfc/blob/luds/01.md
 */

public class LnurlDecoder {

    public static String decode(String data) throws NoLnUrlDataException {

        if (data == null) {
            throw new NoLnUrlDataException("LNURL decoding failed: The data to decode is not a LNURL");
        }

        // Remove the "lightning:" uri scheme if it is present
        data = UriUtil.removeURI(data);

        if (data.length() < 6 || !data.substring(0, 6).toLowerCase().equals("lnurl1")) {
            throw new NoLnUrlDataException("LNURL decoding failed: The data to decode is not a LNURL");
        }

        String decodedLnurl = null;
        try {
            byte[] decodedBech32 = Bech32.bech32Decode(data, false).second;
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
