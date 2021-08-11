package zapsolutions.zap.connection;

import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import zapsolutions.zap.tor.TorManager;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.ZapLog;

/**
 * Singleton to handle the okHttp client
 */
public class HttpClient {
    private static HttpClient mHttpClientInstance;
    private OkHttpClient mHttpClient;
    private static final String LOG_TAG = HttpClient.class.getName();


    private HttpClient() {
        mHttpClient = createHttpClient();
    }

    private OkHttpClient createHttpClient() {
        if (PrefsUtil.isTorEnabled()) {
            Proxy proxyTest = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", TorManager.getInstance().getProxyPort()));

            return new OkHttpClient.Builder()
                    .proxy(proxyTest)
                    .build();
        } else {
            return new OkHttpClient();
        }
    }

    public void restartHttpClient() {
        if (PrefsUtil.isTorEnabled()) {
            ZapLog.d(LOG_TAG, "HttpClient restarted. Proxy Port: " + TorManager.getInstance().getProxyPort());
        } else {
            ZapLog.d(LOG_TAG, "HttpClient restarted.");
        }
        mHttpClient.dispatcher().cancelAll();
        mHttpClient = createHttpClient();
    }

    public static synchronized HttpClient getInstance() {
        if (mHttpClientInstance == null) {
            mHttpClientInstance = new HttpClient();
        }
        return mHttpClientInstance;
    }

    public OkHttpClient getClient() {
        return mHttpClient;
    }
}
