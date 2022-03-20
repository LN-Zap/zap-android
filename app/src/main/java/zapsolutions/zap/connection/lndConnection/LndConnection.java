package zapsolutions.zap.connection.lndConnection;


import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import zapsolutions.zap.BuildConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.lnd.LndAutopilotService;
import zapsolutions.zap.lnd.LndChainNotifierService;
import zapsolutions.zap.lnd.LndInvoicesService;
import zapsolutions.zap.lnd.LndLightningService;
import zapsolutions.zap.lnd.LndRouterService;
import zapsolutions.zap.lnd.LndSignerService;
import zapsolutions.zap.lnd.LndStateService;
import zapsolutions.zap.lnd.LndVersionerService;
import zapsolutions.zap.lnd.LndWalletKitService;
import zapsolutions.zap.lnd.LndWalletUnlockerService;
import zapsolutions.zap.lnd.LndWatchtowerClientService;
import zapsolutions.zap.lnd.LndWatchtowerService;
import zapsolutions.zap.lnd.RemoteLndAutopilotService;
import zapsolutions.zap.lnd.RemoteLndChainNotifierService;
import zapsolutions.zap.lnd.RemoteLndInvoicesService;
import zapsolutions.zap.lnd.RemoteLndLightningService;
import zapsolutions.zap.lnd.RemoteLndRouterService;
import zapsolutions.zap.lnd.RemoteLndSignerService;
import zapsolutions.zap.lnd.RemoteLndStateService;
import zapsolutions.zap.lnd.RemoteLndVersionerService;
import zapsolutions.zap.lnd.RemoteLndWalletKitService;
import zapsolutions.zap.lnd.RemoteLndWalletUnlockerService;
import zapsolutions.zap.lnd.RemoteLndWatchtowerClientService;
import zapsolutions.zap.lnd.RemoteLndWatchtowerService;
import zapsolutions.zap.tor.TorManager;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

/**
 * Singleton to handle the connection to lnd
 */
public class LndConnection {

    private static final String LOG_TAG = LndConnection.class.getName();

    private static LndConnection mLndConnectionInstance;

    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;
    private LndAutopilotService mLndAutopilotService;
    private LndChainNotifierService mLndChainNotifierService;
    private LndInvoicesService mLndInvoicesService;
    private LndLightningService mLndLightningService;
    private LndRouterService mLndRouterService;
    private LndSignerService mLndSignerService;
    private LndStateService mLndStateService;
    private LndVersionerService mLndVersionerService;
    private LndWalletKitService mLndWalletKitService;
    private LndWalletUnlockerService mLndWalletUnlockerService;
    private LndWatchtowerService mLndWatchtowerService;
    private LndWatchtowerClientService mLndWatchtowerClientService;
    private NodeConfig mConnectionConfig;
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

    public LndAutopilotService getAutopilotService() {
        return mLndAutopilotService;
    }

    public LndChainNotifierService getChainNotifierService() {
        return mLndChainNotifierService;
    }

    public LndInvoicesService getInvoicesService() {
        return mLndInvoicesService;
    }

    public LndLightningService getLightningService() {
        return mLndLightningService;
    }

    public LndRouterService getRouterService() {
        return mLndRouterService;
    }

    public LndSignerService getSignerService() {
        return mLndSignerService;
    }

    public LndStateService getStateService() {
        return mLndStateService;
    }

    public LndVersionerService getVersionerService() {
        return mLndVersionerService;
    }

    public LndWalletKitService getWalletKitService() {
        return mLndWalletKitService;
    }

    public LndWalletUnlockerService getWalletUnlockerService() {
        return mLndWalletUnlockerService;
    }

    public LndWatchtowerService getWatchtowerService() {
        return mLndWatchtowerService;
    }

    public LndWatchtowerClientService getWatchtowerClientService() {
        return mLndWatchtowerClientService;
    }

    private void readSavedConnectionInfo() {

        // Load current wallet connection config
        mConnectionConfig = NodeConfigsManager.getInstance().getCurrentNodeConfig();

        // Generate Macaroon
        mMacaroon = new MacaroonCallCredential(mConnectionConfig.getMacaroon());
    }

    private void generateChannelAndStubs() {
        String host = mConnectionConfig.getHost();
        int port = mConnectionConfig.getPort();

        HostnameVerifier hostnameVerifier = null;
        if (BuildConfig.BUILD_TYPE.equals("debug") || mConnectionConfig.isTor()) {
            // Disable hostname verification on debug build variant. This is is used to prevent connection errors to REGTEST nodes.
            // On Tor we do not need it, as tor already makes sure we are connected with the correct host.
            hostnameVerifier = new BlindHostnameVerifier();
        }

        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        if (PrefsUtil.isTorEnabled()) {
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .proxyDetector(new ZapTorProxyDetector(TorManager.getInstance().getProxyPort()))//
                    .hostnameVerifier(hostnameVerifier) // null = default hostnameVerifier
                    .sslSocketFactory(LndSSLSocketFactory.create(mConnectionConfig)) // null = default SSLSocketFactory
                    .build();
        } else {
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .hostnameVerifier(hostnameVerifier) // null = default hostnameVerifier
                    .sslSocketFactory(LndSSLSocketFactory.create(mConnectionConfig)) // null = default SSLSocketFactory
                    .build();
        }

        mLndAutopilotService = new RemoteLndAutopilotService(mSecureChannel, mMacaroon);
        mLndChainNotifierService = new RemoteLndChainNotifierService(mSecureChannel, mMacaroon);
        mLndInvoicesService = new RemoteLndInvoicesService(mSecureChannel, mMacaroon);
        mLndLightningService = new RemoteLndLightningService(mSecureChannel, mMacaroon);
        mLndRouterService = new RemoteLndRouterService(mSecureChannel, mMacaroon);
        mLndSignerService = new RemoteLndSignerService(mSecureChannel, mMacaroon);
        mLndStateService = new RemoteLndStateService(mSecureChannel, mMacaroon);
        mLndVersionerService = new RemoteLndVersionerService(mSecureChannel, mMacaroon);
        mLndWalletKitService = new RemoteLndWalletKitService(mSecureChannel, mMacaroon);
        mLndWatchtowerService = new RemoteLndWatchtowerService(mSecureChannel, mMacaroon);
        mLndWatchtowerClientService = new RemoteLndWatchtowerClientService(mSecureChannel, mMacaroon);
        mLndWalletUnlockerService = new RemoteLndWalletUnlockerService(mSecureChannel, mMacaroon);
    }

    public void openConnection() {
        if (!isConnected) {
            isConnected = true;
            ZapLog.d(LOG_TAG, "Starting LND connection...(Open Http Channel)");
            readSavedConnectionInfo();
            generateChannelAndStubs();
            Wallet.getInstance().checkIfLndIsUnlockedAndConnect();
        }

    }

    public void closeConnection() {
        if (mSecureChannel != null) {
            ZapLog.d(LOG_TAG, "Shutting down LND connection...(Closing Http Channel)");
            shutdownChannel();
        }

        isConnected = false;
    }

    public void reconnect() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
            closeConnection();
            openConnection();
        }
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
            if (mSecureChannel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS)) {
                ZapLog.d(LOG_TAG, "LND channel shutdown successfully...");
                Wallet.getInstance().setLNDAsDisconnected();
            } else {
                ZapLog.e(LOG_TAG, "LND channel shutdown failed...");
            }
        } catch (InterruptedException e) {
            ZapLog.e(LOG_TAG, "LND channel shutdown exception: " + e.getMessage());
        }
    }

    public NodeConfig getConnectionConfig() {
        return mConnectionConfig;
    }

}
