package ln_zap.zap.connection;


import android.content.SharedPreferences;

import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.google.common.io.BaseEncoding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSocketFactory;

import androidx.preference.PreferenceManager;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import ln_zap.zap.baseClasses.App;

/**
 * Singleton to handle the connection to lnd
 *
 * Please note:
 * IP, Certificate and Macaroon are placeholders right now.
 */
public class LndConnection {
    private static LndConnection mLndConnectionInstance;
    private SSLSocketFactory mSSLFactory;
    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;
    private ExecutorService mLndThreads;
    private boolean mIsShutdown = false;
    private LightningGrpc.LightningBlockingStub mBlockingClient;
    private SharedPreferences mPrefs;


    private LndConnection() {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());

        // Macaroon
        String macaroonBase64UrlString = mPrefs.getString("remoteMacaroon","");
        byte[] macaroonBytes = BaseEncoding.base64Url().decode(macaroonBase64UrlString);
        String macaroon = BaseEncoding.base16().encode(macaroonBytes);
        mMacaroon = new MacaroonCallCredential(macaroon);

        // SSL
        String certificateBase64UrlString = mPrefs.getString("remoteCert","");
        byte[] certificateBytes = BaseEncoding.base64Url().decode(certificateBase64UrlString);

        mSSLFactory = CustomSSLSocketFactory.create(certificateBytes);

        generateChannelAndStubs();

        /* Create threads for up to 5 concurrent LND requests. If more request than 5 Request want
         to be executed they wait in a queue until one thread is available. These threads can not
         modify the UI */
        // mLndThreads = Executors.newFixedThreadPool(5);
    }

    private void generateChannelAndStubs(){
        String host = mPrefs.getString("remoteHost","");
        int port = mPrefs.getInt("remotePort",10009);
        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        mSecureChannel = OkHttpChannelBuilder
                .forAddress(host,port)
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

    public void stopBackgroundTasks(){
        // Close the OKHttpChannel
        mSecureChannel.shutdownNow();
        // Shutdown the thread pool for lnd requests
        //mLndThreads.shutdownNow();
        mIsShutdown = true;
    }

    public void restartBackgroundTasks(){
        generateChannelAndStubs();

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

}
