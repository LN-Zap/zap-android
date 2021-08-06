package zapsolutions.zap.connection;

import okhttp3.OkHttpClient;

/**
 * Singleton to handle the volley http RequestQueue
 */
public class HttpClientOk {
    private static HttpClientOk mHttpClientInstance;
    private OkHttpClient mHttpClient;


    private HttpClientOk() {
        mHttpClient = new OkHttpClient();
    }


    public static synchronized HttpClientOk getInstance() {
        if (mHttpClientInstance == null) {
            mHttpClientInstance = new HttpClientOk();
        }
        return mHttpClientInstance;
    }

    public OkHttpClient getClient() {
        return mHttpClient;
    }
}
