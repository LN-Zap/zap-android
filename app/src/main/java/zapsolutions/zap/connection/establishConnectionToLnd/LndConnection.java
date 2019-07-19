package zapsolutions.zap.connection.establishConnectionToLnd;


import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.google.common.io.BaseEncoding;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.util.ZapLog;

import javax.net.ssl.SSLSocketFactory;

import java.util.concurrent.ExecutorService;

/**
 * Singleton to handle the connection to lnd
 */
public class LndConnection {

    private static final String LOG_TAG = "LND Connection";

    private static LndConnection mLndConnectionInstance;
    private SSLSocketFactory mSSLFactory;
    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;
    private ExecutorService mLndThreads;
    private boolean mIsShutdown = false;
    private LightningGrpc.LightningBlockingStub mBlockingClient;
    private WalletConfig mConnectionConfig;


    private LndConnection() {
        readSavedConnectionInfo();
    }

    public static synchronized LndConnection getInstance() {
        if (mLndConnectionInstance == null) {
            mLndConnectionInstance = new LndConnection();
        }
        return mLndConnectionInstance;
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

        generateChannelAndStubs();

        /* Create threads for up to 5 concurrent LND requests. If more request than 5 Request want
         to be executed they wait in a queue until one thread is available. These threads can not
         modify the UI */
        // mLndThreads = Executors.newFixedThreadPool(5);
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

        // Blocking client to for sync gRPC calls
        mBlockingClient = LightningGrpc
                .newBlockingStub(mSecureChannel)
                .withCallCredentials(mMacaroon);

    }

    public void stopBackgroundTasks() {
        // Close the OKHttpChannel
        mSecureChannel.shutdownNow();
        // Shutdown the thread pool for lnd requests
        //mLndThreads.shutdownNow();
        mIsShutdown = true;
    }

    public void restartBackgroundTasks() {
        readSavedConnectionInfo();

        // Shutdown the thread pool for lnd requests
        //if (mIsShutdown){
        //   mLndThreads = Executors.newFixedThreadPool(5);
        //}
    }


    public MacaroonCallCredential getMacaroon() {
        return mMacaroon;
    }

    public ManagedChannel getSecureChannel() {
        return mSecureChannel;
    }

    public ExecutorService getLndThreads() {
        return mLndThreads;
    }

    public LightningGrpc.LightningBlockingStub getBlockingClient() {
        return mBlockingClient;
    }

    public WalletConfig getConnectionConfig() {
        return mConnectionConfig;
    }


}
