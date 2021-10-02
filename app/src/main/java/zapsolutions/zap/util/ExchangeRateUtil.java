package zapsolutions.zap.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.HttpClient;

public class ExchangeRateUtil {

    private static final String LOG_TAG = ExchangeRateUtil.class.getName();

    private static final String BLOCKCHAIN_INFO = "Blockchain.info";
    private static final String COINBASE = "Coinbase";
    private static final String RATE = "rate";
    private static final String SYMBOL = "symbol";
    private static final String TIMESTAMP = "timestamp";

    private final Set<ExchangeRateUtil.ExchangeRateListener> mExchangeRateListeners = new HashSet<>();
    private Context mContext;
    private static ExchangeRateUtil mInstance;


    private ExchangeRateUtil() {
        mContext = App.getAppContext();
    }

    public static ExchangeRateUtil getInstance() {
        if (mInstance == null) {
            mInstance = new ExchangeRateUtil();
        }

        return mInstance;
    }

    public void getExchangeRates() {

        if (!MonetaryUtil.getInstance().getSecondCurrency().isBitcoin() ||
                !PrefsUtil.getPrefs().contains(PrefsUtil.AVAILABLE_FIAT_CURRENCIES)) {

            String provider = PrefsUtil.getPrefs().getString(PrefsUtil.EXCHANGE_RATE_PROVIDER, BLOCKCHAIN_INFO);


            switch (provider) {
                case BLOCKCHAIN_INFO:
                    sendBlockchainInfoRequest();
                    break;
                case COINBASE:
                    sendCoinbaseRequest();
                    break;
                default:
                    sendBlockchainInfoRequest();
            }

            ZapLog.v(LOG_TAG, "Exchange rate request initiated");
        }
    }


    /**
     * Creates and sends a request that fetches fiat exchange rate data from "blockchain.info".
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     */
    private void sendBlockchainInfoRequest() {

        Request rateRequest = new Request.Builder()
                .url("https://blockchain.info/ticker")
                .build();

        HttpClient.getInstance().getClient().newCall(rateRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ZapLog.w(LOG_TAG, "Fetching exchange rates from blockchain.info failed");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ZapLog.v(LOG_TAG, "Received exchange rates from blockchain.info");
                String responseData = response.body().string();
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseData);
                } catch (JSONException e) {
                    ZapLog.w(LOG_TAG, "blockchain.info response could not be parsed as json");
                    e.printStackTrace();
                    if (responseData.toLowerCase().contains("cloudflare") && responseData.toLowerCase().contains("captcha-bypass")) {
                        broadcastExchangeRateUpdateFailed(ExchangeRateListener.ERROR_CLOUDFLARE_BLOCKED_TOR, RefConstants.ERROR_DURATION_VERY_LONG);
                    }
                }
                if (responseJson != null) {
                    JSONObject responseRates = parseBlockchainInfoResponse(responseJson);
                    applyExchangeRatesAndSaveInPreferences(responseRates);
                }
            }
        });
    }

    /**
     * Creates and sends a request that fetches fiat exchange rate data from Coinbase.
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     */
    private void sendCoinbaseRequest() {

        Request rateRequest = new Request.Builder()
                .url("https://api.coinbase.com/v2/exchange-rates?currency=BTC")
                .build();

        HttpClient.getInstance().getClient().newCall(rateRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ZapLog.w(LOG_TAG, "Fetching exchange rates from coinbase failed");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ZapLog.v(LOG_TAG, "Received exchange rates from coinbase");
                String responseData = response.body().string();
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseData);
                } catch (JSONException e) {
                    ZapLog.w(LOG_TAG, "Coinbase response could not be parsed as json");
                    e.printStackTrace();
                    if (responseData.toLowerCase().contains("cloudflare") && responseData.toLowerCase().contains("captcha-bypass")) {
                        broadcastExchangeRateUpdateFailed(ExchangeRateListener.ERROR_CLOUDFLARE_BLOCKED_TOR, RefConstants.ERROR_DURATION_VERY_LONG);
                    }
                }
                if (responseJson != null) {
                    JSONObject responseRates = parseCoinbaseResponse(responseJson);
                    applyExchangeRatesAndSaveInPreferences(responseRates);
                }
            }
        });
    }

    /**
     * This function parses a blockchain.info exchange rate response.
     * All the response parser functions return a similar formatted JSON Object
     * {"USD":{"rate":0.1231, "timestamp":...},"EUR":{...}}
     *
     * @param response a JSON response that comes from Blockchain.info
     * @return
     */
    public JSONObject parseBlockchainInfoResponse(JSONObject response) {

        JSONObject formattedRates = new JSONObject();
        // loop through all returned currencies
        Iterator<String> iter = response.keys();
        while (iter.hasNext()) {
            String fiatCode = iter.next();
            try {
                JSONObject ReceivedCurrency = response.getJSONObject(fiatCode);
                JSONObject currentCurrency = new JSONObject();
                currentCurrency.put(RATE, ReceivedCurrency.getDouble("15m") / 1e8);
                currentCurrency.put(SYMBOL, ReceivedCurrency.getString("symbol"));
                currentCurrency.put(TIMESTAMP, System.currentTimeMillis() / 1000);
                formattedRates.put(fiatCode, currentCurrency);
            } catch (JSONException e) {
                ZapLog.e(LOG_TAG, "Unable to read exchange rate from blockchain.info response.");
            }
        }

        // Switch order as blockchain.info has USD first. We want to have it alphabetically.
        try {
            JSONObject tempCurrency = formattedRates.getJSONObject("USD");
            formattedRates.remove("USD");
            formattedRates.put("USD", tempCurrency);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return formattedRates;
    }

    /**
     * This function parses a coinbase exchange rate response.
     * All the response parser functions return a similar formatted JSON Object
     * {"USD":{"rate":0.1231, "timestamp":...},"EUR":{...}}
     *
     * @param response a JSON response that comes from Coinbases API
     * @return
     */
    public JSONObject parseCoinbaseResponse(JSONObject response) {

        JSONObject responseRates = null;
        try {
            responseRates = response.getJSONObject("data").getJSONObject("rates");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject formattedRates = new JSONObject();
        if (responseRates != null) {
            Iterator<String> iter = responseRates.keys();
            while (iter.hasNext()) {
                String rateCode = iter.next();

                try {
                    JSONObject currentCurrency = new JSONObject();
                    currentCurrency.put(RATE, responseRates.getDouble(rateCode) / 1e8);
                    currentCurrency.put(TIMESTAMP, System.currentTimeMillis() / 1000);
                    formattedRates.put(rateCode, currentCurrency);
                } catch (JSONException e) {
                    ZapLog.e(LOG_TAG, "Unable to read exchange rate from coinbase response.");
                }
            }
        } else {
            return null;
        }

        return formattedRates;
    }

    /**
     * Some responses include exchange rates to other crypto currencies.
     * This function removes them by checking if the currency code is in our fiat currency list.
     */
    private JSONObject removeNonFiat(JSONObject rates) {

        // Load the currency list from JSON file.
        JSONObject fiatList = null;
        String currencies = AppUtil.getInstance(mContext).loadJSONFromResource(R.raw.currency_list);

        try {
            fiatList = new JSONObject(currencies);
        } catch (JSONException e) {
            ZapLog.e(LOG_TAG, "Error reading currency_list JSON: " + e.getMessage());
        }

        // Remove all exchange rates that are not in the list
        if (fiatList != null) {
            Iterator<String> iter = rates.keys();
            while (iter.hasNext()) {
                String rateCode = iter.next();
                if (!fiatList.has(rateCode)) {
                    iter.remove();
                }
            }
        }
        return rates;
    }


    private void applyExchangeRatesAndSaveInPreferences(JSONObject exchangeRates) {

        final SharedPreferences.Editor editor = PrefsUtil.editPrefs();
        editor.remove(PrefsUtil.AVAILABLE_FIAT_CURRENCIES);
        editor.commit();

        JSONArray availableCurrenciesArray = new JSONArray();


        // loop through all returned currencies and save them in the preferences.
        Iterator<String> iter = exchangeRates.keys();
        while (iter.hasNext()) {
            String rateCode = iter.next();
            try {
                JSONObject tempRate = exchangeRates.getJSONObject(rateCode);
                availableCurrenciesArray.put(rateCode);
                editor.putString("fiat_" + rateCode, tempRate.toString());

                // Update the current fiat currency of the Monetary util
                if (rateCode.equals(PrefsUtil.getSecondCurrency())) {
                    if (tempRate.has(SYMBOL)) {
                        MonetaryUtil.getInstance().setSecondCurrency(rateCode, tempRate.getDouble(RATE), tempRate.getLong(TIMESTAMP), tempRate.getString(SYMBOL));
                    } else {
                        MonetaryUtil.getInstance().setSecondCurrency(rateCode, tempRate.getDouble(RATE), tempRate.getLong(TIMESTAMP));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // JSON Object that will hold all available currencies to later populate selection list in the settings.
        JSONObject availableCurrencies = new JSONObject();

        try {
            // Save the codes of all found currencies in a JSON object, which will then be stored on shared preferences
            availableCurrencies.put("currencies", availableCurrenciesArray);
            editor.putString(PrefsUtil.AVAILABLE_FIAT_CURRENCIES, availableCurrencies.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editor.commit();
        setDefaultCurrency();
        broadcastExchangeRateUpdate();
        ZapLog.v(LOG_TAG, "Exchange rate is: " + MonetaryUtil.getInstance().getSecondCurrency().getRate() * 1E8);
    }

    private void setDefaultCurrency() {
        // If this was the first time executed since installation, automatically set the
        // currency to correct currency according to the systems locale. Only do this,
        // if this currency is included in the fetched data.
        // The user might also have changed the provider and his currency is no longer available. Also switch to default in this case.
        if (!PrefsUtil.getPrefs().getBoolean(PrefsUtil.IS_DEFAULT_CURRENCY_SET, false) || !isCurrencyAvailable(PrefsUtil.getSecondCurrency())) {
            String currencyCode = AppUtil.getInstance(mContext).getSystemCurrencyCode();
            if (currencyCode != null) {
                if (!PrefsUtil.getPrefs().getString("fiat_" + currencyCode, "").isEmpty()) {
                    final SharedPreferences.Editor editor = PrefsUtil.editPrefs();
                    editor.putBoolean(PrefsUtil.IS_DEFAULT_CURRENCY_SET, true);
                    editor.putString(PrefsUtil.SECOND_CURRENCY, currencyCode);
                    editor.commit();
                    MonetaryUtil.getInstance().loadSecondCurrencyFromPrefs(currencyCode);
                }
            }
        }
    }

    private boolean isCurrencyAvailable(String currency) {
        try {
            JSONObject jsonAvailableCurrencies = new JSONObject(PrefsUtil.getPrefs().getString(PrefsUtil.AVAILABLE_FIAT_CURRENCIES, PrefsUtil.DEFAULT_FIAT_CURRENCIES));
            JSONArray currencies = jsonAvailableCurrencies.getJSONArray("currencies");

            for (int i = 0, count = currencies.length(); i < count; i++) {
                if (currencies.getString(i).equals(currency)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            ZapLog.e(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
        }
        return false;
    }

    // Event handling to notify all registered listeners to an exchange rate change.

    private void broadcastExchangeRateUpdate() {
        for (ExchangeRateUtil.ExchangeRateListener listener : mExchangeRateListeners) {
            listener.onExchangeRatesUpdated();
        }
    }

    private void broadcastExchangeRateUpdateFailed(int error, int duration) {
        for (ExchangeRateUtil.ExchangeRateListener listener : mExchangeRateListeners) {
            listener.onExchangeRateUpdateFailed(error, duration);
        }
    }

    public void registerExchangeRateListener(ExchangeRateUtil.ExchangeRateListener listener) {
        mExchangeRateListeners.add(listener);
    }

    public void unregisterExchangeRateListener(ExchangeRateUtil.ExchangeRateListener listener) {
        mExchangeRateListeners.remove(listener);
    }

    public interface ExchangeRateListener {

        int ERROR_CLOUDFLARE_BLOCKED_TOR = 0;

        void onExchangeRatesUpdated();

        void onExchangeRateUpdateFailed(int error, int duration);
    }

}
