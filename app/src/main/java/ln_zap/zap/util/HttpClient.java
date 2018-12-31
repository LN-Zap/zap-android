package ln_zap.zap.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class HttpClient {
    private static HttpClient mHttpClientInstance;
    private RequestQueue mRequestQueue;


    private HttpClient(Context ctx) {
        mRequestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
    }


    public static synchronized HttpClient getInstance(Context ctx) {
        if (mHttpClientInstance == null) {
            mHttpClientInstance = new HttpClient(ctx);
        }
        return mHttpClientInstance;
    }


    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req,String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }


    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
