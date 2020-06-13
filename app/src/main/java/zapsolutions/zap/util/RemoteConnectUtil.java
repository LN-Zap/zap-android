package zapsolutions.zap.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfig;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfigParser;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectConfig;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectStringParser;

public class RemoteConnectUtil {

    private static final String LOG_TAG = RemoteConnectUtil.class.getName();

    public static void decodeConnectionString(Context ctx, String data, OnRemoteConnectDecodedListener listener) {
        if (UriUtil.isLNDConnectUri(data)) {
            decodeLndConnectString(ctx, data, listener);
        } else if (data.startsWith("config=")) {
            // URL to BTCPayConfigJson
            String configUrl = data.replace("config=", "");
            StringRequest btcPayConfigRequest = new StringRequest(Request.Method.GET, configUrl,
                    response -> decodeBtcPay(ctx, response, listener),
                    error -> listener.onError(ctx.getResources().getString(R.string.error_unableToFetchBTCPayConfig), RefConstants.ERROR_DURATION_SHORT));

            ZapLog.debug(LOG_TAG, "Fetching BTCPay config...");
            HttpClient.getInstance().addToRequestQueue(btcPayConfigRequest, "BTCPayConfigRequest");
        } else if (BTCPayConfigParser.isValidJson(data)) {
            // Valid BTCPay JSON
            decodeBtcPay(ctx, data, listener);
        } else {
            listener.onNoConnectData();
        }
    }

    private static void decodeLndConnectString(Context ctx, String data, OnRemoteConnectDecodedListener listener) {
        LndConnectStringParser parser = new LndConnectStringParser(data).parse();

        if (parser.hasError()) {
            switch (parser.getError()) {
                case LndConnectStringParser.ERROR_INVALID_CONNECT_STRING:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_invalidLndConnectString), RefConstants.ERROR_DURATION_LONG);
                    break;
                case LndConnectStringParser.ERROR_NO_MACAROON:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_no_macaroon), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
                case LndConnectStringParser.ERROR_INVALID_CERTIFICATE:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_invalid_certificate), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
                case LndConnectStringParser.ERROR_INVALID_MACAROON:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_invalid_macaroon), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
                case LndConnectStringParser.ERROR_INVALID_HOST_OR_PORT:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_invalid_host_or_port), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
            }
        } else {
            listener.onValidLndConnectString(parser.getConnectionConfig());
        }
    }

    private static void decodeBtcPay(Context ctx, @NonNull String btcPayConfigurationJson, OnRemoteConnectDecodedListener listener) {
        BTCPayConfigParser btcPayConfigParser = new BTCPayConfigParser(btcPayConfigurationJson).parse();

        if (btcPayConfigParser.hasError()) {
            switch (btcPayConfigParser.getError()) {
                case BTCPayConfigParser.ERROR_INVALID_JSON:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_btcpay_invalid_json), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
                case BTCPayConfigParser.ERROR_MISSING_BTC_GRPC_CONFIG:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_btcpay_invalid_config), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
                case BTCPayConfigParser.ERROR_NO_MACAROON:
                    listener.onError(ctx.getResources().getString(R.string.error_connection_no_macaroon), RefConstants.ERROR_DURATION_MEDIUM);
                    break;
            }
        } else {
            // Parsing was successful
            listener.onValidBTCPayConnectData(btcPayConfigParser.getConnectionConfig());
        }
    }


    public static void saveRemoteConfiguration(RemoteConfiguration config, OnSaveRemoteConfigurationListener listener) {

        WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();

        try {
            if (config instanceof LndConnectConfig) {
                LndConnectConfig lndConfig = (LndConnectConfig) config;

                String id = walletConfigsManager.addWalletConfig(config.getHost(),
                        WalletConfig.WALLET_TYPE_REMOTE, lndConfig.getHost(), lndConfig.getPort(),
                        lndConfig.getCert(), lndConfig.getMacaroon()).getId();

                walletConfigsManager.apply();

                listener.onSaved(id);

            } else if (config instanceof BTCPayConfig) {
                BTCPayConfig btcPayConfig = (BTCPayConfig) config;

                String id = walletConfigsManager.addWalletConfig(config.getHost(),
                        WalletConfig.WALLET_TYPE_REMOTE, btcPayConfig.getHost(), btcPayConfig.getPort(),
                        null, btcPayConfig.getMacaroon()).getId();

                walletConfigsManager.apply();

                listener.onSaved(id);

            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getMessage(), RefConstants.ERROR_DURATION_SHORT);
        }
    }


    public interface OnRemoteConnectDecodedListener {

        void onValidLndConnectString(RemoteConfiguration remoteConfiguration);

        void onValidBTCPayConnectData(RemoteConfiguration remoteConfiguration);

        void onNoConnectData();

        void onError(String error, int duration);
    }

    public interface OnSaveRemoteConfigurationListener {

        void onSaved(String walletId);

        void onError(String error, int duration);
    }
}
