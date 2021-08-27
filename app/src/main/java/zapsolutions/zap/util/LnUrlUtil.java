package zapsolutions.zap.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.lnurl.LnUrlResponse;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.lnurl.channel.LnUrlHostedChannelResponse;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.lnurl.withdraw.LnUrlWithdrawResponse;

public class LnUrlUtil {
    private static final String LOG_TAG = LnUrlUtil.class.getName();

    public static void readLnUrl(Context ctx, String data, OnLnUrlReadListener listener) {
        // Extract fallback LNURL from URL if one is present
        try {
            URL url = new URL(data);
            String query = url.getQuery();
            if (query != null && query.contains("lightning=LNURL1")) {
                data = UtilFunctions.getQueryParam(url, "lightning");
            }
        } catch (MalformedURLException ignored) {
        }

        // Check the full data or the extracted LNURL from above to see if it is a valid LNURL
        try {
            String decodedLnUrl = LnurlDecoder.decode(data);
            ZapLog.v(LOG_TAG, "Decoded LNURL: " + decodedLnUrl);
            // Check if it has a query param called login. In this case do not make a GET request as the AuthFlow works different.
            URL decodedUrl = null;
            try {
                decodedUrl = new URL(decodedLnUrl);
                String query = decodedUrl.getQuery();
                if (query != null && query.contains("tag=login")) {
                    String k1 = UtilFunctions.getQueryParam(decodedUrl, "k1");
                    if (k1 != null && k1.length() == 64 && UtilFunctions.isHex(k1)) {
                        listener.onValidLnUrlAuth(decodedUrl);
                    } else {
                        listener.onError(ctx.getString(R.string.lnurl_decoding_no_lnurl_data), RefConstants.ERROR_DURATION_MEDIUM);
                    }
                    return;
                }
            } catch (MalformedURLException e) {
                listener.onError(ctx.getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_MEDIUM);
            }

            ZapLog.v(LOG_TAG, "LNURL: Requesting data...");

            Request lnurlRequest = new Request.Builder()
                    .url(decodedLnUrl)
                    .build();

            HttpClient.getInstance().getClient().newCall(lnurlRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    URL url = null;
                    try {
                        url = new URL(decodedLnUrl);
                        String host = url.getHost();
                        listener.onError(ctx.getString(R.string.lnurl_service_not_responding, host), RefConstants.ERROR_DURATION_SHORT);
                    } catch (MalformedURLException error) {
                        String host = ctx.getString(R.string.host);
                        listener.onError(ctx.getString(R.string.lnurl_service_not_responding, host), RefConstants.ERROR_DURATION_SHORT);
                        error.printStackTrace();
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    interpretLnUrlReadResponse(response.body().string(), listener, ctx);
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            ZapLog.e(LOG_TAG, "LNURL is invalid. Decoding failed.");
            listener.onError(ctx.getString(R.string.lnurl_decoding_no_lnurl_data), RefConstants.ERROR_DURATION_MEDIUM);
        } catch (LnurlDecoder.NoLnUrlDataException e) {
            listener.onNoLnUrlData();
        }
    }

    private static void interpretLnUrlReadResponse(@NonNull String response, OnLnUrlReadListener listener, Context ctx) {
        LnUrlResponse lnUrlResponse;
        try {
            lnUrlResponse = new Gson().fromJson(response, LnUrlResponse.class);
        } catch (JsonSyntaxException e) {
            ZapLog.e(LOG_TAG, "LNURL was successfully decoded, but the response when actually calling the decoded LNURL was not readable as JSON.");
            listener.onError(ctx.getString(R.string.lnurl_decoding_no_lnurl_data), RefConstants.ERROR_DURATION_MEDIUM);
            return;
        }

        if (lnUrlResponse.hasError()) {
            ZapLog.w(LOG_TAG, "LNURL: Request invalid. Reason: " + lnUrlResponse.getReason());
            listener.onError(lnUrlResponse.getReason(), RefConstants.ERROR_DURATION_MEDIUM);
        } else {
            if (lnUrlResponse.isWithdraw()) {
                ZapLog.d(LOG_TAG, "LNURL: valid withdraw data received...");
                LnUrlWithdrawResponse lnUrlWithdrawResponse = new Gson().fromJson(response, LnUrlWithdrawResponse.class);
                listener.onValidLnUrlWithdraw(lnUrlWithdrawResponse);
            } else if (lnUrlResponse.isPayRequest()) {
                ZapLog.d(LOG_TAG, "LNURL: valid pay request data received...");
                LnUrlPayResponse lnUrlPayResponse = new Gson().fromJson(response, LnUrlPayResponse.class);
                listener.onValidLnUrlPay(lnUrlPayResponse);
            } else if (lnUrlResponse.isChannelRequest()) {
                ZapLog.d(LOG_TAG, "LNURL: valid channel request data received...");
                LnUrlChannelResponse lnUrlChannelResponse = new Gson().fromJson(response, LnUrlChannelResponse.class);
                listener.onValidLnUrlChannel(lnUrlChannelResponse);
            } else if (lnUrlResponse.isHostedChannelRequest()) {
                ZapLog.d(LOG_TAG, "LNURL: valid hosted channel request data received...");
                LnUrlHostedChannelResponse lnUrlHostedChannelResponse = new Gson().fromJson(response, LnUrlHostedChannelResponse.class);
                listener.onValidLnUrlHostedChannel(lnUrlHostedChannelResponse);
            } else {
                ZapLog.w(LOG_TAG, "LNURL: valid but unsupported data received...");
                listener.onError(ctx.getString(R.string.lnurl_unsupported_type), RefConstants.ERROR_DURATION_MEDIUM);
            }
        }
    }


    public interface OnLnUrlReadListener {

        void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse);

        void onValidLnUrlPay(LnUrlPayResponse payResponse);

        void onValidLnUrlChannel(LnUrlChannelResponse channelResponse);

        void onValidLnUrlHostedChannel(LnUrlHostedChannelResponse hostedChannelResponse);

        void onValidLnUrlAuth(URL url);

        void onError(String error, int duration);

        void onNoLnUrlData();
    }

}
