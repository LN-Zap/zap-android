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
            JsonObjectRequest request;

            switch (provider) {
                case BLOCKCHAIN_INFO:
                    request = getBlockchainInfoRequest();
                    break;
                case COINBASE:
                    request = getCoinbaseRequest();
                    break;
                default:
                    request = getBlockchainInfoRequest();
            }

            if (request != null) {
                // Adding request to request queue
                HttpClient.getInstance().addToRequestQueue(request, "rateRequest");
                ZapLog.v(LOG_TAG, "Exchange rate request initiated");
            }
        }
    }


    /**
     * Creates a Request that fetches fiat exchange rate data from "blockchain.info".
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     *
     * @return JsonObjectRequest
     */
    private JsonObjectRequest getBlockchainInfoRequest() {
        JsonObjectRequest rateRequest = new JsonObjectRequest(Request.Method.GET, "https://blockchain.info/ticker", null,
                response -> {
                    ZapLog.v(LOG_TAG, "Received exchange rates from blockchain.info");
                    JSONObject responseRates = parseBlockchainInfoResponse(response);
                    applyExchangeRatesAndSaveInPreferences(responseRates);
                }, error -> ZapLog.w(LOG_TAG, "Fetching exchange rates from blockchain.info failed"));

        return rateRequest;
    }

    /**
     * Creates a Request that fetches fiat exchange rate data from Coinbase.
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     *
     * @return JsonObjectRequest
     */
    private JsonObjectRequest getCoinbaseRequest() {
        JsonObjectRequest rateRequest = new JsonObjectRequest(Request.Method.GET, "https://api.coinbase.com/v2/exchange-rates?currency=BTC", null,
                response -> {
                    ZapLog.v(LOG_TAG, "Received exchange rates from coinbase");
                    JSONObject responseRates = parseCoinbaseResponse(response);
                    applyExchangeRatesAndSaveInPreferences(removeNonFiat(responseRates));
                }, error -> ZapLog.w(LOG_TAG, "Fetching exchange rates from coinbase failed"));

        return rateRequest;
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

        final SharedPreferences.Editor editor = PrefsUtil.edit();
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
                    final SharedPreferences.Editor editor = PrefsUtil.edit();
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
