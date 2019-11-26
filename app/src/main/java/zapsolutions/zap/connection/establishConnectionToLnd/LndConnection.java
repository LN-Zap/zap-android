package zapsolutions.zap.connection.establishConnectionToLnd;


import com.google.common.io.BaseEncoding;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.lnd.LndLightningService;
import zapsolutions.zap.lnd.LndWalletUnlockerService;
import zapsolutions.zap.lnd.RemoteLndLightningService;
import zapsolutions.zap.lnd.RemoteLndWalletUnlockerService;
import zapsolutions.zap.util.ZapLog;

/**
 * Singleton to handle the connection to lnd
 */
public class LndConnection {

    private static final String LOG_TAG = LndConnection.class.getName();

    private static LndConnection mLndConnectionInstance;

    private SSLSocketFactory mSSLFactory;
    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;
    private LndLightningService mLndLightningService;
    private LndWalletUnlockerService mLndWalletUnlockerService;
    private WalletConfig mConnectionConfig;
    private boolean isConnected = false;

    private LndConnection() {
        ;
    }

    public static synchronized LndConnection getInstance() {
        if (mLndConnectionInstance == null) {
            mLndConnectionInstance = new LndConnection();
        }
        return mLndConnectionInstance;
    }

    public LndLightningService getLightningService() {
        return mLndLightningService;
    }

    public LndWalletUnlockerService getWalletUnlockerService() {
        return mLndWalletUnlockerService;
    }

    private void readSavedConnectionInfo() {

        // Load current wallet connection config
        mConnectionConfig = WalletConfigsManager.getInstance().getCurrentWalletConfig();

        // Generate Macaroon
        mMacaroon = new MacaroonCallCredential(mConnectionConfig.getMacaroon());

        mSSLFactory = null;

        // Generate certificate if one was supplied
        if (mConnectionConfig.getCert() != null) {
            // We have a certificate, try to load it.

            String certificateBase64UrlString = mConnectionConfig.getCert();
            byte[] certificateBytes = BaseEncoding.base64Url().decode(certificateBase64UrlString);

            try {
                mSSLFactory = CustomSSLSocketFactory.create(certificateBytes);
            } catch (RuntimeException e) {
                ZapLog.debug(LOG_TAG, "Error on Certificate");
            }

        }
    }

    private void generateChannelAndStubs() {
        String host = mConnectionConfig.getHost();
        int port = mConnectionConfig.getPort();

        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        if (mSSLFactory == null) {
            // BTCPay
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .build();

        } else {
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .sslSocketFactory(mSSLFactory)
                    .build();
        }

        mLndLightningService = new RemoteLndLightningService(mSecureChannel, mMacaroon);
        mLndWalletUnlockerService = new RemoteLndWalletUnlockerService(mSecureChannel, mMacaroon);
    }

    public void openConnection() {
        if (!isConnected) {
            isConnected = true;
            ZapLog.debug(LOG_TAG, "Starting LND connection...(Open Http Channel)");
            readSavedConnectionInfo();
            generateChannelAndStubs();
        }

    }

    public void closeConnection() {
        if (mSecureChannel != null) {
            ZapLog.debug(LOG_TAG, "Shutting down LND connection...(Closing Http Channel)");
            shutdownChannel();
        }

        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Will shutdown the channel and cancel all active calls.
     * Waits for shutdown (blocking) and logs result.
     */
    private void shutdownChannel() {
        try {
            if (mSecureChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS)) {
                ZapLog.debug(LOG_TAG, "LND channel shutdown successfully...");
            } else {
                ZapLog.debug(LOG_TAG, "LND channel shutdown failed...");
            }
        } catch (InterruptedException e) {
            ZapLog.debug(LOG_TAG, "LND channel shutdown exception: " + e.getMessage());
        }
    }

    public WalletConfig getConnectionConfig() {
        return mConnectionConfig;
    }

}
