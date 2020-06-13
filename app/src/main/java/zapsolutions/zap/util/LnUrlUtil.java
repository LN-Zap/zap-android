package zapsolutions.zap.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.lnurl.LnUrlResponse;
import zapsolutions.zap.lnurl.LnUrlWithdrawResponse;

public class LnUrlUtil {
    private static final String LOG_TAG = LnUrlUtil.class.getName();

    public static void readLnUrl(Context ctx, String data, OnLnUrlReadListener listener) {
        try {
            String decodedLnUrl = LnurlDecoder.decode(data);

            // Check if it has a query param called login. In this case do not make a GET request as the AuthFlow works different.
            URL decodedUrl = null;
            try {
                decodedUrl = new URL(decodedLnUrl);
                if (decodedUrl.getQuery().contains("tag=login")) {
                    listener.onError(ctx.getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_MEDIUM);
                    return;
                }
            } catch (MalformedURLException e) {
                listener.onError(ctx.getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_MEDIUM);
            }

            StringRequest lnurlRequest = new StringRequest(Request.Method.GET, decodedLnUrl,
                    response -> interpretLnUrlReadResponse(response, listener, ctx),
                    error -> {
                        URL url = null;
                        try {
                            url = new URL(decodedLnUrl);
                            String host = url.getHost();
                            listener.onError(ctx.getString(R.string.lnurl_service_not_responding, host), RefConstants.ERROR_DURATION_SHORT);
                        } catch (MalformedURLException e) {
                            String host = ctx.getString(R.string.host);
                            listener.onError(ctx.getString(R.string.lnurl_service_not_responding, host), RefConstants.ERROR_DURATION_SHORT);
                            e.printStackTrace();
                        }
                    });

            ZapLog.debug(LOG_TAG, "LNURL: Requesting data...");
            HttpClient.getInstance().addToRequestQueue(lnurlRequest, "LnUrlWithdrawRequest");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (LnurlDecoder.NoLnUrlDataException e) {
            listener.onNoLnUrlData();
        }
    }

    private static void interpretLnUrlReadResponse(@NonNull String response, OnLnUrlReadListener listener, Context ctx) {
        LnUrlResponse lnUrlResponse = new Gson().fromJson(response, LnUrlResponse.class);

        if (lnUrlResponse.hasError()) {
            ZapLog.debug(LOG_TAG, "LNURL: Request invalid. Reason: " + lnUrlResponse.getReason());
            listener.onError(lnUrlResponse.getReason(), RefConstants.ERROR_DURATION_MEDIUM);
        } else {
            if (lnUrlResponse.isWithdraw()) {
                ZapLog.debug(LOG_TAG, "LNURL: valid withdraw data received...");
                LnUrlWithdrawResponse lnUrlWithdrawResponse = new Gson().fromJson(response, LnUrlWithdrawResponse.class);
                listener.onValidLnUrlWithdraw(lnUrlWithdrawResponse);
            } else if (lnUrlResponse.isPayRequest()) {
                // ToDo: Implement pay request response
                listener.onValidLnUrlPayRequest();
            } else {
                ZapLog.debug(LOG_TAG, "LNURL: valid but unsupported data received...");
                listener.onError(ctx.getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_MEDIUM);
            }
        }
    }


    public interface OnLnUrlReadListener {

        void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse);

        void onValidLnUrlPayRequest();

        void onError(String error, int duration);

        void onNoLnUrlData();
    }

}
