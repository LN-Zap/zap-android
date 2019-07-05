package zapsolutions.zap.connection.lndConnect;

import com.google.common.io.BaseEncoding;

import java.net.URI;
import java.net.URISyntaxException;

import zapsolutions.zap.connection.CustomSSLSocketFactory;
import zapsolutions.zap.util.ZapLog;

/**
 * This class parses a lndconnect which is defined in this project:
 * https://github.com/LN-Zap/lndconnect
 * <p>
 * A lndconnect string consists of the following parts:
 * lndconnect://<HOST>:<PORT>?cert=<certificate_encoded_as_base64url>&macaroon=<macaroon_encoded_as_base64url>
 * <p>
 * Note: The certificate is not mandatory. For cases like BTCPay server where another certificate is used, this can be omitted.
 * <p>
 * The parser returns an object containing the desired data or an descriptive error.
 */
public class LndConnectStringParser {

    private static final String LOG_TAG = "LND connect string parser";

    public static final int ERROR_INVALID_CONNECT_STRING = 0;
    public static final int ERROR_NO_MACAROON = 1;
    public static final int ERROR_INVALID_CERTIFICATE = 2;
    public static final int ERROR_INVALID_MACAROON = 3;
    public static final int ERROR_INVALID_HOST_OR_PORT = 4;


    public LndConnectStringResult parse(String connectString) {

        LndConnectStringResult result = new LndConnectStringResult();

        // validate not null
        if (connectString == null) {
            result.setError(ERROR_INVALID_CONNECT_STRING);
            return result;
        }

        // validate scheme
        if (!connectString.toLowerCase().startsWith("lndconnect://")) {
            result.setError(ERROR_INVALID_CONNECT_STRING);
            return result;
        }

        URI connectURI = null;
        try {
            connectURI = new URI(connectString);

            // validate host and port
            if (connectURI.getPort() == -1) {
                result.setError(ERROR_INVALID_HOST_OR_PORT);
                return result;
            }

            String cert = null;
            String macaroon = null;

            // fetch params
            if (connectURI.getQuery() != null) {
                String[] valuePairs = connectURI.getQuery().split("&");

                for (String pair : valuePairs) {
                    String[] param = pair.split("=");
                    if (param.length > 1) {
                        if (param[0].equals("cert")) {
                            cert = param[1];
                        }
                        if (param[0].equals("macaroon")) {
                            macaroon = param[1];
                        }
                    }
                }

                // validate cert (Certificate is not mandatory for BTCPay server for example, therefore null is valid)
                if (cert != null) {
                    try {
                        byte[] certificateBytes = BaseEncoding.base64Url().decode(cert);
                        try {
                            CustomSSLSocketFactory.create(certificateBytes);
                        } catch (RuntimeException e) {

                            ZapLog.debug(LOG_TAG, "certificate creation failed");
                            result.setError(ERROR_INVALID_CERTIFICATE);
                            return result;
                        }
                    } catch (IllegalArgumentException e) {
                        ZapLog.debug(LOG_TAG, "cert decoding failed");
                        result.setError(ERROR_INVALID_CERTIFICATE);
                        return result;
                    }
                }

                // validate macaroon if everything was valid so far
                if (macaroon == null) {
                    ZapLog.debug(LOG_TAG, "lnd connect string does not include a macaroon");
                    result.setError(ERROR_NO_MACAROON);
                    return result;
                } else {
                    try {
                        BaseEncoding.base64Url().decode(macaroon);
                    } catch (IllegalArgumentException e) {
                        ZapLog.debug(LOG_TAG, "macaroon decoding failed");

                        result.setError(ERROR_INVALID_MACAROON);
                        return result;
                    }
                }

                // everything is ok, initiate connection
                result.setError(-1);
                result.setHost(connectURI.getHost());
                result.setPort(connectURI.getPort());
                result.setCert(cert);
                result.setMacaroon(macaroon);

                return result;

            } else {
                ZapLog.debug(LOG_TAG, "Connect URI has no parameters");
                result.setError(ERROR_INVALID_CONNECT_STRING);
                return result;
            }

        } catch (URISyntaxException e) {
            ZapLog.debug(LOG_TAG, "URI could not be parsed");
            result.setError(ERROR_INVALID_CONNECT_STRING);
            return result;
        }
    }
}
