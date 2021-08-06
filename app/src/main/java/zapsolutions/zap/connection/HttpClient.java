package zapsolutions.zap.connection;

import okhttp3.OkHttpClient;

/**
 * Singleton to handle the volley http RequestQueue
 */
public class HttpClient {
    private static HttpClient mHttpClientInstance;
    private OkHttpClient mHttpClient;


    private HttpClient() {
        mHttpClient = new OkHttpClient();
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
