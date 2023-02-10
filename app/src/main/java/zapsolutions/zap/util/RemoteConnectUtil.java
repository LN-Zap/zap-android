package zapsolutions.zap.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.BaseNodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfig;
import zapsolutions.zap.connection.parseConnectionData.btcPay.BTCPayConfigParser;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectConfig;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectStringParser;

public class RemoteConnectUtil {

    private static final String LOG_TAG = RemoteConnectUtil.class.getName();

    public static void decodeConnectionString(Context ctx, String data, OnRemoteConnectDecodedListener listener) {
        if (data == null) {
            listener.onNoConnectData();
            return;
        }
        if (UriUtil.isLNDConnectUri(data)) {
            decodeLndConnectString(ctx, data, listener);
        } else if (data.startsWith("config=")) {
            // URL to BTCPayConfigJson
            String configUrl = data.replace("config=", "");

            Request btcPayConfigRequest = new Request.Builder()
                    .url(configUrl)
                    .build();

            HttpClient.getInstance().getClient().newCall(btcPayConfigRequest).enqueue(new Callback() {
                Handler threadHandler = new Handler(Looper.getMainLooper());

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    threadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(ctx.getResources().getString(R.string.error_unableToFetchBTCPayConfig), RefConstants.ERROR_DURATION_SHORT);
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    threadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                decodeBtcPay(ctx, response.body().string(), listener);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
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


    public static void saveRemoteConfiguration(Context ctx, BaseNodeConfig config, @Nullable String walletUUID, OnSaveRemoteConfigurationListener listener) {
        int port = config.getPort();
        if (port == 8080) {
            // Zap Android does not support REST. If the REST port was supplied, we ask the user if he wants to change it to 10009 (gRPC port).
            new AlertDialog.Builder(ctx)
                    .setMessage(R.string.rest_port)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            executeSaveRemoteConfiguration(config, walletUUID, 10009, listener);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executeSaveRemoteConfiguration(config, walletUUID, port, listener);
                        }
                    }).show();
        } else {
            executeSaveRemoteConfiguration(config, walletUUID, port, listener);
        }
    }

    private static void executeSaveRemoteConfiguration(BaseNodeConfig config, @Nullable String walletUUID, int port, OnSaveRemoteConfigurationListener listener) {
        NodeConfigsManager nodeConfigsManager = NodeConfigsManager.getInstance();

        try {
            if (config instanceof LndConnectConfig) {
                LndConnectConfig lndConfig = (LndConnectConfig) config;

                String id;
                if (walletUUID == null) {

                    if (nodeConfigsManager.doesDestinationExist(lndConfig.getHost(), port)) {
                        listener.onAlreadyExists();
                        return;
                    }

                    id = nodeConfigsManager.addNodeConfig(lndConfig.getHost(),
                            ZapNodeConfig.NODE_TYPE_REMOTE, BaseNodeConfig.NODE_IMPLEMENTATION_LND, lndConfig.getHost(), port,
                            lndConfig.getCert(), lndConfig.getMacaroon(), lndConfig.getUseTor(), lndConfig.getVerifyCertificate()).getId();
                } else {
                    id = walletUUID;
                    String oldAlias = nodeConfigsManager.getNodeConfigById(id).getAlias();
                    nodeConfigsManager.updateNodeConfig(id, oldAlias,
                            ZapNodeConfig.NODE_TYPE_REMOTE, BaseNodeConfig.NODE_IMPLEMENTATION_LND, lndConfig.getHost(), port,
                            lndConfig.getCert(), lndConfig.getMacaroon(), lndConfig.getUseTor(), lndConfig.getVerifyCertificate());
                }

                nodeConfigsManager.apply();

                listener.onSaved(id);

            } else if (config instanceof BTCPayConfig) {
                BTCPayConfig btcPayConfig = (BTCPayConfig) config;

                String id;
                if (walletUUID == null) {

                    if (nodeConfigsManager.doesDestinationExist(btcPayConfig.getHost(), port)) {
                        listener.onAlreadyExists();
                        return;
                    }

                    id = nodeConfigsManager.addNodeConfig(btcPayConfig.getHost(),
                            ZapNodeConfig.NODE_TYPE_REMOTE, BaseNodeConfig.NODE_IMPLEMENTATION_LND, btcPayConfig.getHost(), port,
                            null, btcPayConfig.getMacaroon(), btcPayConfig.getUseTor(), btcPayConfig.getVerifyCertificate()).getId();
                } else {
                    id = walletUUID;
                    String oldAlias = nodeConfigsManager.getNodeConfigById(id).getAlias();
                    nodeConfigsManager.updateNodeConfig(id, oldAlias,
                            ZapNodeConfig.NODE_TYPE_REMOTE, BaseNodeConfig.NODE_IMPLEMENTATION_LND, btcPayConfig.getHost(), port,
                            null, btcPayConfig.getMacaroon(), btcPayConfig.getUseTor(), btcPayConfig.getVerifyCertificate());
                }

                nodeConfigsManager.apply();

                listener.onSaved(id);

            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getMessage(), RefConstants.ERROR_DURATION_SHORT);
        }
    }

    public static boolean isTorHostAddress(String Host) {
        if (Host == null) {
            return false;
        }
        return Host.toLowerCase().endsWith(".onion");
    }


    public interface OnRemoteConnectDecodedListener {

        void onValidLndConnectString(BaseNodeConfig baseNodeConfig);

        void onValidBTCPayConnectData(BaseNodeConfig baseNodeConfig);

        void onNoConnectData();

        void onError(String error, int duration);
    }

    public interface OnSaveRemoteConfigurationListener {

        void onSaved(String walletId);

        void onAlreadyExists();

        void onError(String error, int duration);
    }
}
