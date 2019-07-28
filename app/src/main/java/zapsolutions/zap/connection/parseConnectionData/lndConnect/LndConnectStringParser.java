package zapsolutions.zap.connection.parseConnectionData.lndConnect;

import com.google.common.io.BaseEncoding;
import zapsolutions.zap.connection.establishConnectionToLnd.CustomSSLSocketFactory;
import zapsolutions.zap.connection.parseConnectionData.BaseConnectionParser;
import zapsolutions.zap.util.ZapLog;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class parses a lndconnect which is defined in this project:
 * https://github.com/LN-Zap/lndconnect
 * <p>
 * A lndconnect string consists of the following parts:
 * lndconnect://<HOST>:<PORT>?cert=<certificate_encoded_as_base64url>&macaroon=<macaroon_encoded_as_base64url>
 * <p>
 * Note: For lndconnect a certificate is not mandatory.
 * <p>
 * The parser returns an object containing the desired data or an descriptive error.
 */
public class LndConnectStringParser extends BaseConnectionParser<LndConnectConfig> {

    public static final int ERROR_INVALID_CONNECT_STRING = 0;
    public static final int ERROR_NO_MACAROON = 1;
    public static final int ERROR_INVALID_CERTIFICATE = 2;
    public static final int ERROR_INVALID_MACAROON = 3;
    public static final int ERROR_INVALID_HOST_OR_PORT = 4;
    private static final String LOG_TAG = LndConnectStringParser.class.getName();

    public LndConnectStringParser(String connectString) {
        super(connectString);
    }

    public LndConnectStringParser parse() {

        // validate not null
        if (mConnectionString == null) {
            mError = ERROR_INVALID_CONNECT_STRING;
            return this;
        }

        // validate scheme
        if (!mConnectionString.toLowerCase().startsWith("lndconnect://")) {
            mError = ERROR_INVALID_CONNECT_STRING;
            return this;
        }

        URI connectURI = null;
        try {
            connectURI = new URI(mConnectionString);

            // validate host and port
            if (connectURI.getPort() == -1) {
                mError = ERROR_INVALID_HOST_OR_PORT;
                return this;
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

                // validate cert (Certificate is not mandatory)
                if (cert != null) {
                    try {
                        byte[] certificateBytes = BaseEncoding.base64Url().decode(cert);
                        try {
                            CustomSSLSocketFactory.create(certificateBytes);
                        } catch (RuntimeException e) {

                            ZapLog.debug(LOG_TAG, "certificate creation failed");
                            mError = ERROR_INVALID_CERTIFICATE;
                            return this;
                        }
                    } catch (IllegalArgumentException e) {
                        ZapLog.debug(LOG_TAG, "cert decoding failed");
                        mError = ERROR_INVALID_CERTIFICATE;
                        return this;
                    }
                }

                // validate macaroon if everything was valid so far
                if (macaroon == null) {
                    ZapLog.debug(LOG_TAG, "lnd connect string does not include a macaroon");
                    mError = ERROR_NO_MACAROON;
                    return this;
                } else {
                    try {
                        BaseEncoding.base64Url().decode(macaroon);
                        byte[] macaroonBytes = BaseEncoding.base64Url().decode(macaroon);
                        macaroon = BaseEncoding.base16().encode(macaroonBytes);
                    } catch (IllegalArgumentException e) {
                        ZapLog.debug(LOG_TAG, "macaroon decoding failed");

                        mError = ERROR_INVALID_MACAROON;
                        return this;
                    }
                }

                // everything is ok, initiate connection
                LndConnectConfig lndConnectConfig = new LndConnectConfig();
                lndConnectConfig.setHost(connectURI.getHost());
                lndConnectConfig.setPort(connectURI.getPort());
                lndConnectConfig.setCert(cert);
                lndConnectConfig.setMacaroon(macaroon);
                setConnectionConfig(lndConnectConfig);
                return this;

            } else {
                ZapLog.debug(LOG_TAG, "Connect URI has no parameters");
                mError = ERROR_INVALID_CONNECT_STRING;
                return this;
            }

        } catch (URISyntaxException e) {
            ZapLog.debug(LOG_TAG, "URI could not be parsed");
            mError = ERROR_INVALID_CONNECT_STRING;
            return this;
        }
    }
}
