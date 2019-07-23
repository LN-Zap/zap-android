package zapsolutions.zap.connection.parseConnectionData.btcPay;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import zapsolutions.zap.connection.parseConnectionData.BaseConnectionParser;
import zapsolutions.zap.util.ZapLog;

/**
 * This class parses a BTCPayConfig JSON which is defined in this project:
 * https://github.com/btcpayserver/btcpayserver
 * <p>
 * Note: A certificate is not mandatory.
 * <p>
 * The parser returns an object containing the desired data or an descriptive error.
 */
public class BTCPayConfigParser extends BaseConnectionParser<BTCPayConfig> {

    public static final int ERROR_INVALID_JSON = 0;
    public static final int ERROR_MISSING_BTC_GRPC_CONFIG = 1;
    public static final int ERROR_NO_MACAROON = 2;
    public static final int ERROR_INVALID_HOST_OR_PORT = 3;

    private static final String LOG_TAG = "BTCPayConfigParser";

    public BTCPayConfigParser(String connectString) {
        super(connectString);
    }

    /**
     * Used to determine if the provided String is a valid BTCPayConfiguration JSON.
     *
     * @param connectionString parses as JSON
     * @return if the JSON syntax is valid
     */
    public static boolean isValidJson(String connectionString) {
        try {
            BTCPayConfigJson btcPayConfigJson = new Gson().fromJson(connectionString, BTCPayConfigJson.class);
            return btcPayConfigJson != null;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    public BTCPayConfigParser parse() {

        BTCPayConfigJson btcPayConfigJson;

        try {
            btcPayConfigJson = new Gson().fromJson(mConnectionString, BTCPayConfigJson.class);
        } catch (JsonSyntaxException ex) {
            ZapLog.debug(LOG_TAG, "BTCPay Configuration JSON syntax is invalid");
            mError = ERROR_INVALID_JSON;
            return this;
        }

        BTCPayConfig configuration = btcPayConfigJson.getConfiguration(BTCPayConfig.TYPE_GRPC, BTCPayConfig.CRYPTO_TYPE_BTC);

        if (configuration == null) {
            ZapLog.debug(LOG_TAG, "BTCPay Configuration does not contain BTC gRPC config");
            mError = ERROR_MISSING_BTC_GRPC_CONFIG;
            return this;
        }

        // validate host and port
        if (configuration.getPort() == 0 || configuration.getHost().isEmpty()) {
            ZapLog.debug(LOG_TAG, "BTCPay Configuration does not contain a host or port");
            mError = ERROR_INVALID_HOST_OR_PORT;
            return this;
        }

        // validate macaroon if everything was valid so far
        if (configuration.getMacaroon().isEmpty()) {
            ZapLog.debug(LOG_TAG, "BTCPay Configuration does not include a default macaroon");
            mError = ERROR_NO_MACAROON;
            return this;
        }

        setConnectionConfig(configuration);
        return this;
    }
}
