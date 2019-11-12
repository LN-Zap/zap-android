package zapsolutions.zap.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import zapsolutions.zap.baseClasses.App;

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

    public JsonObjectRequest getExchangeRates() {
        String provider = PrefsUtil.getPrefs().getString("exchangeRateProvider", BLOCKCHAIN_INFO);

        ZapLog.debug(LOG_TAG, "Exchange rate request initiated");

        switch (provider) {
            case BLOCKCHAIN_INFO:
                return fromBlockchainInfo();
            case COINBASE:
                return fromCoinbase();
            default:
                return fromBlockchainInfo();

        }

    }


    /**
     * Creates a Request that fetches fiat exchange rate data from "blockchain.info".
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     *
     * @return JsonObjectRequest
     */
    private JsonObjectRequest fromBlockchainInfo() {
        JsonObjectRequest rateRequest = new JsonObjectRequest(Request.Method.GET, "https://blockchain.info/ticker", null,
                response -> {
                    ZapLog.debug(LOG_TAG, "received exchange rates from blockchain.info");
                    JSONObject responseRates = parseBlockchainInfoResponse(response);
                    applyExchangeRatesAndSaveInPreferences(responseRates);
                }, error -> ZapLog.debug(LOG_TAG, "Fetching exchange rates from blockchain.info failed"));

        return rateRequest;
    }

    /**
     * Creates a Request that fetches fiat exchange rate data from Coinbase.
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     *
     * @return JsonObjectRequest
     */
    private JsonObjectRequest fromCoinbase() {
        JsonObjectRequest rateRequest = new JsonObjectRequest(Request.Method.GET, "https://api.coinbase.com/v2/exchange-rates?currency=BTC", null,
                response -> {
                    ZapLog.debug(LOG_TAG, "received exchange rates from coinbase");
                    JSONObject responseRates = parseCoinbaseResponse(response);
                    applyExchangeRatesAndSaveInPreferences(responseRates);
                }, error -> ZapLog.debug(LOG_TAG, "Fetching exchange rates from coinbase failed"));

        return rateRequest;
    }


    private void setDefaultCurrencyOnFirstStart() {
        // If this was the first time executed since installation, automatically set the
        // currency to correct currency according to the systems locale. Only do this,
        // if this currency is included in the fetched data.
        if (!PrefsUtil.getPrefs().getBoolean("isDefaultCurrencySet", false)) {
            String currencyCode = AppUtil.getInstance(mContext).getSystemCurrencyCode();
            if (currencyCode != null) {
                if (!PrefsUtil.getPrefs().getString("fiat_" + currencyCode, "").equals("")) {
                    MonetaryUtil.getInstance().loadSecondCurrencyFromPrefs(currencyCode);
                    final SharedPreferences.Editor editor = PrefsUtil.edit();
                    editor.putBoolean("isDefaultCurrencySet", true);
                    editor.putString("secondCurrency", currencyCode);
                    editor.apply();
                }
            }
        }
    }

    /**
     * This function parses a blockchain.info exchange rate response.
     * All the response parser functions return a similar formatted JSON Object
     * {USD:{rate=0.1231, timestamp=...},EUR:{...}}
     *
     * @param response a JSON response that comes from Blockchain.info
     * @return
     */
    private JSONObject parseBlockchainInfoResponse(JSONObject response) {

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
                ZapLog.debug(LOG_TAG, "Unable to read exchange rate from blockchain.info response.");
            }
        }

        return formattedRates;
    }

    /**
     * This function parses a coinbase exchange rate response.
     * All the response parser functions return a similar formatted JSON Object
     * {USD:{rate=0.1231, timestamp=...},EUR:{...}}
     *
     * @param response a JSON response that comes from Coinbases API
     * @return
     */
    private JSONObject parseCoinbaseResponse(JSONObject response) {

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
                    ZapLog.debug(LOG_TAG, "Unable to read exchange rate from coinbase response.");
                }
            }
            return formattedRates;
        } else {
            return null;
        }
    }


    private void applyExchangeRatesAndSaveInPreferences(JSONObject exchangeRates) {

        final SharedPreferences.Editor editor = PrefsUtil.edit();
        editor.remove("fiat_available");
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
                if (rateCode.equals(PrefsUtil.getPrefs().getString("secondCurrency", "USD"))) {
                    MonetaryUtil.getInstance().setSecondCurrency(rateCode, tempRate.getDouble(RATE), tempRate.getLong(TIMESTAMP), tempRate.getString(SYMBOL));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // JSON Object that will hold all available currencies to later populate selection list in the settings.
        JSONObject availableCurrencies = new JSONObject();

        // Switch the order. Blockchain.info has USD first, we want to have it alphabetically.
        if (PrefsUtil.getPrefs().getString("exchangeRateProvider", BLOCKCHAIN_INFO).equals(BLOCKCHAIN_INFO)) {
            availableCurrenciesArray.remove(0);
            availableCurrenciesArray.put("USD");
        }

        try {
            // Save the codes of all found currencies in a JSON object, which will then be stored on shared preferences
            availableCurrencies.put("currencies", availableCurrenciesArray);
            editor.putString("fiat_available", availableCurrencies.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editor.commit();
        setDefaultCurrencyOnFirstStart();
        broadcastExchangeRateUpdate();
    }


    // Event handling to notify all registered listeners to an exchange rate change.

    private void broadcastExchangeRateUpdate() {
        for (ExchangeRateUtil.ExchangeRateListener listener : mExchangeRateListeners) {
            listener.onExchangeRatesUpdated();
        }
    }

    public void registerExchangeRateListener(ExchangeRateUtil.ExchangeRateListener listener) {
        mExchangeRateListeners.add(listener);
    }

    public void unregisterExchangeRateListener(ExchangeRateUtil.ExchangeRateListener listener) {
        mExchangeRateListeners.remove(listener);
    }

    public interface ExchangeRateListener {
        void onExchangeRatesUpdated();
    }

}
