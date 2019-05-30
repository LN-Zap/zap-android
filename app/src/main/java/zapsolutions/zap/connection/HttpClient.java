package zapsolutions.zap.connection;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import zapsolutions.zap.baseClasses.App;

/**
 * Singleton to handle the volley http RequestQueue
 */
public class HttpClient {
    private static HttpClient mHttpClientInstance;
    private RequestQueue mRequestQueue;


    private HttpClient() {
        mRequestQueue = Volley.newRequestQueue(App.getAppContext());
    }


    public static synchronized HttpClient getInstance() {
        if (mHttpClientInstance == null) {
            mHttpClientInstance = new HttpClient();
        }
        return mHttpClientInstance;
    }


    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }


    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
