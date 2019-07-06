package zapsolutions.zap.connection;


import android.content.SharedPreferences;

import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.google.common.io.BaseEncoding;

import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLSocketFactory;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.UtilFunctions;
import zapsolutions.zap.util.ZapLog;

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
    private String[] mConnectionInfo;
    private SharedPreferences mPrefsRemote;


    private LndConnection() {
        readSavedConnectionInfo();
    }

    private void readSavedConnectionInfo() {

        App ctx = App.getAppContext();

        mPrefsRemote = Armadillo.create(ctx, PrefsUtil.PREFS_REMOTE)
                .encryptionFingerprint(ctx)
                .keyStretchingFunction(new PBKDF2KeyStretcher(RefConstants.NUM_HASH_ITERATIONS, null))
                .password(ctx.inMemoryPin.toCharArray())
                .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                .build();

        // The following string contains host,port,cert and macaroon in one string separated with ";"
        // This way we can read all necessary data in one call and do not have to execute the key stretching function 4 times.
        String connectionInfo = mPrefsRemote.getString(PrefsUtil.REMOTE_COMBINED, "");
        mConnectionInfo = connectionInfo.split(";");
        ZapLog.debug(LOG_TAG, connectionInfo);


        // Macaroon
        String macaroonBase64UrlString = mConnectionInfo[3];
        byte[] macaroonBytes = BaseEncoding.base64Url().decode(macaroonBase64UrlString);
        String macaroon = BaseEncoding.base16().encode(macaroonBytes);
        mMacaroon = new MacaroonCallCredential(macaroon);

        // SSL
        String certificateBase64UrlString = mConnectionInfo[2];
        byte[] certificateBytes = BaseEncoding.base64Url().decode(certificateBase64UrlString);

        try {
            mSSLFactory = CustomSSLSocketFactory.create(certificateBytes);
        } catch (RuntimeException e) {
            ZapLog.debug(LOG_TAG, "Error on Certificate");
        }

        generateChannelAndStubs();

        /* Create threads for up to 5 concurrent LND requests. If more request than 5 Request want
         to be executed they wait in a queue until one thread is available. These threads can not
         modify the UI */
        // mLndThreads = Executors.newFixedThreadPool(5);
    }

    private void generateChannelAndStubs() {

        String host = mConnectionInfo[0];
        int port = Integer.parseInt(mConnectionInfo[1]);
        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        mSecureChannel = OkHttpChannelBuilder
                .forAddress(host, port)
                .sslSocketFactory(mSSLFactory)
                .build();

        // Blocking client to for sync gRPC calls
        mBlockingClient = LightningGrpc
                .newBlockingStub(mSecureChannel)
                .withCallCredentials(mMacaroon);

    }

    public static synchronized LndConnection getInstance() {
        if (mLndConnectionInstance == null) {
            mLndConnectionInstance = new LndConnection();
        }
        return mLndConnectionInstance;
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

    public String[] getConnectionInfo() {
        return mConnectionInfo;
    }

}
