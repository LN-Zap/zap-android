package zapsolutions.zap.lnurl.staticInternetIdentifier;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

/**
 * Please refer to the following specification
 * https://github.com/fiatjaf/lnurl-rfc/blob/luds/16.md
 */

public class StaticInternetIdentifier {

    public static final String ARGS_KEY = "staticInternetIdentifier";
    private static final String LOG_TAG = StaticInternetIdentifier.class.getName();

    public static String IdentifierToRequest(String address) {
        String[] parts = address.split("@");
        String username = parts[0];
        String domain = parts[1];
        if (address.toLowerCase().endsWith(".onion")) {
            return "http://" + domain + "/.well-known/lnurlp/" + username;
        } else {
            return "https://" + domain + "/.well-known/lnurlp/" + username;
        }
    }

    public static void checkIfValidStaticInternetIdentifier(Context ctx, String address, OnStaticIdentifierChecked listener) {
        if (!validateFormat(address)) {
            listener.onNoStaticInternetIdentifierData();
            return;
        }
        String requestUrl = IdentifierToRequest(address);

        okhttp3.Request lightningAddressRequest = new okhttp3.Request.Builder()
                .url(requestUrl)
                .build();

        HttpClient.getInstance().getClient().newCall(lightningAddressRequest).enqueue(new Callback() {
            // We need to make sure the results are executed on the UI Thread to prevent crashes.
            Handler threadHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ZapLog.e(LOG_TAG, e.getMessage());
                listener.onError(e.getMessage(), RefConstants.ERROR_DURATION_SHORT);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isBlockedByCloudflare = false;
                        try {
                            String responseAsString = response.body().string();
                            isBlockedByCloudflare = responseAsString.toLowerCase().contains("cloudflare") && responseAsString.toLowerCase().contains("captcha-bypass");
                            ZapLog.d(LOG_TAG, responseAsString);
                            LnUrlPayResponse lnUrlPayResponse = new Gson().fromJson(responseAsString, LnUrlPayResponse.class);
                            if (lnUrlPayResponse.hasError()) {
                                listener.onError(lnUrlPayResponse.getReason(), RefConstants.ERROR_DURATION_MEDIUM);
                                return;
                            }
                            listener.onValidInternetIdentifier(lnUrlPayResponse);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (isBlockedByCloudflare) {
                                listener.onError(ctx.getResources().getString(R.string.error_tor_blocked_lnurl, address.split("@")[1]), RefConstants.ERROR_DURATION_VERY_LONG);
                            } else {
                                listener.onError(ctx.getResources().getString(R.string.error_static_identifier_response_unknown, address.split("@")[1]), RefConstants.ERROR_DURATION_MEDIUM);
                            }
                        }
                    }
                });
            }
        });
    }

    private static boolean validateFormat(String address) {
        /* Simplified regex checking the following:
            - the string has no white spaces
            - username is a lowercase alphanumeric (including "." and "_") string of at least one character
            - username is followed by exactly one "@"
            - domain name has at leas one "."
            - domain name uses only alphanumeric character (including "-")
        */
        String regexPattern = "^[a-z0-9_.]+@[a-zA-Z0-9-.]+\\.[a-zA-Z0-9-]+$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(address);

        return matcher.matches();
    }

    public interface OnStaticIdentifierChecked {

        void onValidInternetIdentifier(LnUrlPayResponse lnUrlPayResponse);

        void onError(String error, int duration);

        void onNoStaticInternetIdentifierData();
    }
}
