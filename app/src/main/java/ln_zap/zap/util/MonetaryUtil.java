package ln_zap.zap.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import ln_zap.zap.baseClasses.App;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;


/**
 * This Singleton helps to display any value in the desired format.
 */
public class MonetaryUtil {

    private static final String BTC_UNIT = "BTC";
    private static final String MBTC_UNIT = "mBTC";
    private static final String BIT_UNIT = "bit";
    private static final String SATOSHI_UNIT = "sat";

    private static final String LOG_TAG = "MonetaryUtil";

    private static MonetaryUtil mInstance;
    private Context mContext;
    private SharedPreferences mPrefs;
    private Currency mFirstCurrency;
    private Currency mSecondCurrency;

    private final Set<ExchangeRateListener> mExchangeRateListeners = new HashSet<>();


    private MonetaryUtil() {
        mContext = App.getAppContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        loadFirstCurrencyFromPrefs(mPrefs.getString("firstCurrency", "sat"));


        String SecondCurrency = mPrefs.getString("secondCurrency", "USD");
        switch (SecondCurrency) {
            case BTC_UNIT:
                setSecondCurrency(SecondCurrency, 1e-8);
                break;
            case MBTC_UNIT:
                setSecondCurrency(SecondCurrency, 1e-5);
                break;
            case BIT_UNIT:
                setSecondCurrency(SecondCurrency, 1e-2);
                break;
            case SATOSHI_UNIT:
                setSecondCurrency(SecondCurrency, 1d);
                break;
            default:
                // Here we go if the user has selected a fiat currency as second currency.
                if (mPrefs.getString("fiat_" + mPrefs.getString("secondCurrency", "USD"), "").equals("")) {
                    mSecondCurrency = new Currency(mPrefs.getString("secondCurrency", "USD"), 0, 0);
                } else {
                    loadSecondCurrencyFromPrefs(mPrefs.getString("secondCurrency", "USD"));
                }
        }
    }

    public static MonetaryUtil getInstance() {
        if (mInstance == null) {
            mInstance = new MonetaryUtil();
        }

        return mInstance;
    }

    public Currency getFirstCurrency() {
        return mFirstCurrency;
    }

    public Currency getSecondCurrency() {
        return mSecondCurrency;
    }

    /**
     * Get the amount and display unit of the primary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getPrimaryDisplayAmountAndUnit(long value) {
        return getPrimaryDisplayAmount(value) + " " + getPrimaryDisplayUnit();
    }


    /**
     * Get the amount of the primary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getPrimaryDisplayAmount(long value) {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return getFirstCurrencyAmount(value);
        } else {
            return getSecondCurrencyAmount(value);
        }
    }


    /**
     * Get the display unit of the primary currency as properly formatted string.
     *
     * @return formatted string
     */
    public String getPrimaryDisplayUnit() {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return getFirstDisplayUnit();
        } else {
            return getSecondDisplayUnit();
        }
    }


    /**
     * Get the amount and display unit of the secondary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getSecondaryDisplayAmountAndUnit(long value) {
        return getSecondaryDisplayAmount(value) + " " + getSecondaryDisplayUnit();
    }


    /**
     * Get the amount of the secondary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getSecondaryDisplayAmount(long value) {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return getSecondCurrencyAmount(value);
        } else {
            return getFirstCurrencyAmount(value);
        }
    }


    /**
     * Get the display unit of the secondary currency as properly formatted string.
     *
     * @return formatted string
     */
    public String getSecondaryDisplayUnit() {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return getSecondDisplayUnit();
        } else {
            return getFirstDisplayUnit();
        }
    }


    /**
     * This function returns how old our fiat exchange rate data is.
     *
     * @return Age in seconds.
     */
    public long getExchangeRateAge() {
        return (System.currentTimeMillis() / 1000) - mSecondCurrency.getTimestamp();
    }

    /**
     * Load the first currency from the default settings using a currencyCode (BTC, mBTC, ...)
     *
     * @param currencyCode
     */
    public void loadFirstCurrencyFromPrefs(String currencyCode) {
        switch (currencyCode) {
            case BTC_UNIT:
                setFirstCurrency(currencyCode, 1e-8);
                break;
            case MBTC_UNIT:
                setFirstCurrency(currencyCode, 1e-5);
                break;
            case BIT_UNIT:
                setFirstCurrency(currencyCode, 1e-2);
                break;
            case SATOSHI_UNIT:
                setFirstCurrency(currencyCode, 1d);
                break;
            default:
                setFirstCurrency(currencyCode, 1e-8);
        }
    }


    /**
     * Load the second currency from the default settings using a currencyCode (USD, EUR, BTC, ...)
     * By loading it, we have access to it without parsing the JSON string over and over.
     *
     * @param currencyCode (USD, EUR, etc.)
     */
    public void loadSecondCurrencyFromPrefs(String currencyCode) {
        switch (currencyCode) {
            case BTC_UNIT:
                setSecondCurrency(currencyCode, 1e-8);
                break;
            case MBTC_UNIT:
                setSecondCurrency(currencyCode, 1e-5);
                break;
            case BIT_UNIT:
                setSecondCurrency(currencyCode, 1e-2);
                break;
            case SATOSHI_UNIT:
                setSecondCurrency(currencyCode, 1d);
                break;
            default:

                try {
                    JSONObject selectedCurrency = new JSONObject(mPrefs.getString("fiat_" + currencyCode, "{}"));
                    Currency currency;
                    if (selectedCurrency.has("symbol")) {
                        currency = new Currency(currencyCode,
                                selectedCurrency.getDouble("rate"),
                                selectedCurrency.getLong("timestamp"),
                                selectedCurrency.getString("symbol"));
                    } else {
                        currency = new Currency(currencyCode,
                                selectedCurrency.getDouble("rate"),
                                selectedCurrency.getLong("timestamp"));
                    }

                    mSecondCurrency = currency;
                } catch (JSONException e) {
                    // App was probably never started before. If we can't find the fiat in the prefs,
                    // create a placeholder currency.
                    mSecondCurrency = new Currency("USD", 0, 0);
                }
        }
    }


    /**
     * Switch which of the currencies (first or second one) is used as primary currency
     */
    public void switchCurrencies() {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("firstCurrencyIsPrimary", false);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("firstCurrencyIsPrimary", true);
            editor.apply();
        }
    }

    /**
     * Get primary currency object
     *
     * @return
     */
    public Currency getPrimaryCurrency() {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return mFirstCurrency;
        } else {
            return mSecondCurrency;
        }
    }


    /**
     * Get secondary currency object
     *
     * @return
     */
    public Currency getSecondaryCurrency() {
        if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
            return mSecondCurrency;
        } else {
            return mFirstCurrency;
        }
    }

    /**
     * Use this method to switch the input amount of a textfield right before currencies are swapped.
     *
     * @param primaryValue value string of the input field
     * @return value string for the input field converted in the secondary currency
     */
    public String convertPrimaryToSecondaryCurrency(String primaryValue) {
        if (primaryValue.equals("")) {
            return "";
        } else {
            if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
                double value = Double.parseDouble(primaryValue);
                double result = (value / mFirstCurrency.getRate() * mSecondCurrency.getRate());
                DecimalFormat df = TextInputCurrencyFormat(mSecondCurrency);
                return df.format(result);
            } else {
                double value = Double.parseDouble(primaryValue);
                ;
                double result = (value / mSecondCurrency.getRate()) * mFirstCurrency.getRate();
                DecimalFormat df = TextInputCurrencyFormat(mFirstCurrency);
                return df.format(result);
            }
        }
    }

    /**
     * Converts the supplied value to satoshis. The exchange rate of the primary currency is used.
     *
     * @param primaryValue
     * @return String without grouping or fractions
     */
    public String convertPrimaryToSatoshi(String primaryValue) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(0);
        if (primaryValue.equals("")) {
            return "0";
        } else {
            if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
                double value = Double.parseDouble(primaryValue);
                double result = (value / mFirstCurrency.getRate());
                return df.format(result);
            } else {
                double value = Double.parseDouble(primaryValue);
                double result = (value / mSecondCurrency.getRate());
                return df.format(result);
            }
        }
    }

    /**
     * Converts the given satoshis to primary currency.
     *
     * @param value
     * @return String without grouping
     */
    public String convertSatoshiToPrimary(Long value) {

        if (value == 0) {
            return "0";
        } else {
            if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
                double result = (value * mFirstCurrency.getRate());
                DecimalFormat df = TextInputCurrencyFormat(mFirstCurrency);
                return df.format(result);
            } else {
                double result = (value * mSecondCurrency.getRate());
                DecimalFormat df = TextInputCurrencyFormat(mSecondCurrency);
                return df.format(result);
            }
        }
    }

    /**
     * Converts the supplied value to bitcoin. The exchange rate of the primary currency is used.
     *
     * @param primaryValue
     * @return String without grouping and maximum fractions of 8 digits
     */
    public String convertPrimaryToBitcoin(String primaryValue) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(8);

        if (primaryValue.equals("")) {
            return "0";
        } else {
            if (mPrefs.getBoolean("firstCurrencyIsPrimary", true)) {
                double value = Double.parseDouble(primaryValue);
                double result = (value / mFirstCurrency.getRate() / 1e8);
                return df.format(result);
            } else {
                double value = Double.parseDouble(primaryValue);
                double result = (value / mSecondCurrency.getRate() / 1e8);
                return df.format(result);
            }
        }
    }


    /**
     * Checks if a numerical currency input is valid
     *
     * @param input
     * @return boolean
     */
    public boolean validateCurrencyInput(String input, Currency currency) {

        int numberOfDecimals = 0;

        // Bitcoin
        if (currency.isBitcoin()) {

            String btcUnit = mPrefs.getString("firstCurrency", "sat");
            switch (btcUnit) {
                case BTC_UNIT:
                    numberOfDecimals = 8;
                    break;
                case MBTC_UNIT:
                    numberOfDecimals = 5;
                    break;
                case BIT_UNIT:
                    numberOfDecimals = 2;
                    break;
                case SATOSHI_UNIT:
                    numberOfDecimals = 0;
                    break;
                default:
                    numberOfDecimals = 0;
            }
        }

        // Fiat
        else {
            numberOfDecimals = 2;
        }

        // Regex selecting any or no number of digits optionally followed by "." or "," that is followed by up to numberOfDecimals digits
        String regexPattern = "[0-9]*([\\.,]{0,1}[0-9]{0," + numberOfDecimals + "})";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }


    private void setFirstCurrency(String currencyCode, Double rate) {
        mFirstCurrency = new Currency(currencyCode, rate);
    }

    private void setFirstCurrency(String currencyCode, Double rate, Long timestamp) {
        mFirstCurrency = new Currency(currencyCode, rate, timestamp);
    }

    private void setFirstCurrency(String currencyCode, Double rate, Long timestamp, String symbol) {
        mFirstCurrency = new Currency(currencyCode, rate, timestamp, symbol);
    }

    private void setSecondCurrency(String currencyCode, Double rate) {
        mSecondCurrency = new Currency(currencyCode, rate);
    }

    private void setSecondCurrency(String currencyCode, Double rate, Long timestamp) {
        mSecondCurrency = new Currency(currencyCode, rate, timestamp);
    }

    private void setSecondCurrency(String currencyCode, Double rate, Long timestamp, String symbol) {
        mSecondCurrency = new Currency(currencyCode, rate, timestamp, symbol);
    }


    private String getFirstDisplayUnit() {
        if (mFirstCurrency.getSymbol() == null) {
            return mFirstCurrency.getCode();
        } else {
            return mFirstCurrency.getSymbol();
        }
    }

    private String getSecondDisplayUnit() {
        if (mSecondCurrency.getSymbol() == null) {
            return mSecondCurrency.getCode();
        } else {
            return mSecondCurrency.getSymbol();
        }
    }

    private String getFirstCurrencyAmount(long value) {
        if (mFirstCurrency.isBitcoin()) {
            switch (mFirstCurrency.getCode()) {
                case BTC_UNIT:
                    return formatAsBtcDisplayAmount(value);
                case MBTC_UNIT:
                    return formatAsMbtcDisplayAmount(value);
                case BIT_UNIT:
                    return formatAsBitsDisplayAmount(value);
                case SATOSHI_UNIT:
                    return formatAsSatoshiDisplayAmount(value);
                default:
                    return formatAsBtcDisplayAmount(value);
            }
        } else {
            return getFiatDisplayAmount(value);
        }
    }

    private String getSecondCurrencyAmount(long value) {
        if (mSecondCurrency.isBitcoin()) {
            switch (mSecondCurrency.getCode()) {
                case BTC_UNIT:
                    return formatAsBtcDisplayAmount(value);
                case MBTC_UNIT:
                    return formatAsMbtcDisplayAmount(value);
                case BIT_UNIT:
                    return formatAsBitsDisplayAmount(value);
                case SATOSHI_UNIT:
                    return formatAsSatoshiDisplayAmount(value);
                default:
                    return formatAsBtcDisplayAmount(value);
            }
        } else {
            return getFiatDisplayAmount(value);
        }
    }


    ////////// Bitcoin display functions /////////////


    private String formatAsBtcDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMaximumFractionDigits(8);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        return df.format(value / 1e8);
    }

    private String formatAsMbtcDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMaximumFractionDigits(5);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(19);
        return df.format(value / 100000d);
    }

    private String formatAsBitsDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(22);
        String result = df.format(value / 100d);

        // If we have a fraction, then always show 2 fraction digits for bits
        if (result.contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))) {
            df.setMinimumFractionDigits(2);
            return df.format(value / 100d);
        } else {
            return result;
        }
    }

    private String formatAsSatoshiDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        return df.format(value);
    }


    ////////// Fiat functions /////////////


    private String getFiatDisplayAmount(long value) {
        double fiatValue = (mSecondCurrency.getRate()) * value;
        return formatAsFiatDisplayAmount(fiatValue);
    }


    private String formatAsFiatDisplayAmount(double value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(22);
        String result = df.format(value);
        return result;
    }


    private DecimalFormat TextInputCurrencyFormat(final Currency currency) {
        // We have to use the Locale.US here to ensure Double.parse works correctly later.
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.setGroupingUsed(false);
        if (currency.isBitcoin()) {

            switch (currency.getCode()) {
                case BTC_UNIT:
                    df.setMaximumFractionDigits(8);
                    break;
                case MBTC_UNIT:
                    df.setMaximumFractionDigits(5);
                    break;
                case BIT_UNIT:
                    df.setMaximumFractionDigits(2);
                    break;
                case SATOSHI_UNIT:
                    df.setMaximumFractionDigits(0);
                    break;
                default:
                    df.setMaximumFractionDigits(8);
            }
        } else {
            df.setMaximumFractionDigits(2);
        }
        return df;
    }


    /**
     * Creates a Request that fetches fiat exchange rate data from "blockchain.info".
     * When executed this request saves the result in shared preferences and
     * updates the currentCurrency of the MonetaryUtil Singleton.
     *
     * @return JsonObjectRequest
     */
    public JsonObjectRequest getExchangeRates() {

        // Creating request
        JsonObjectRequest rateRequest = new JsonObjectRequest(Request.Method.GET, "https://blockchain.info/ticker", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        final SharedPreferences.Editor editor = mPrefs.edit();

                        // JSON Object that will hold all available currencies to later populate selection list.
                        JSONObject availableCurrencies = new JSONObject();

                        JSONArray availableCurrenciesArray = new JSONArray();

                        // loop through all returned currencies
                        Iterator<String> iter = response.keys();
                        while (iter.hasNext()) {
                            String fiatCode = iter.next();
                            try {
                                JSONObject ReceivedCurrency = response.getJSONObject(fiatCode);
                                JSONObject FiatCurrency = new JSONObject();
                                FiatCurrency.put("rate", ReceivedCurrency.getDouble("15m") / 1e8);
                                FiatCurrency.put("symbol", ReceivedCurrency.getString("symbol"));
                                FiatCurrency.put("timestamp", System.currentTimeMillis() / 1000);
                                editor.putString("fiat_" + fiatCode, FiatCurrency.toString());
                                availableCurrenciesArray.put(fiatCode);
                                // Update the current fiat currency of the Monetary util
                                if (fiatCode.equals(mPrefs.getString("secondCurrency", "USD"))) {
                                    setSecondCurrency(fiatCode, ReceivedCurrency.getDouble("15m") / 1e8, System.currentTimeMillis() / 1000, ReceivedCurrency.getString("symbol"));
                                }
                            } catch (JSONException e) {
                                ZapLog.debug(LOG_TAG, "Unable to decode currency from fiat exchange rate request");
                            }
                        }
                        try {
                            // Switch the order. Blockchain.info has USD first, we want to have it alphabetically.
                            // ToDO: this is a quick fix, it only works as long as there is no currency alphabetically after USD
                            availableCurrenciesArray.remove(0);
                            availableCurrenciesArray.put("USD");

                            // Save the codes of all found currencies in a JSON object, which will then be stored on shared preferences
                            availableCurrencies.put("currencies", availableCurrenciesArray);
                            editor.putString("fiat_available", availableCurrencies.toString());
                        } catch (JSONException e) {
                            ZapLog.debug(LOG_TAG, "unable to add array to object");
                        }
                        editor.apply();

                        // If this was the first time executed since installation, automatically set the
                        // currency to correct currency according to the systems locale. Only do this,
                        // if this currency is included in the fetched data.
                        if (!mPrefs.getBoolean("isDefaultCurrencySet", false)) {
                            String currencyCode = AppUtil.getInstance(mContext).getSystemCurrencyCode();
                            if (currencyCode != null) {
                                if (!mPrefs.getString("fiat_" + currencyCode, "").equals("")) {
                                    loadSecondCurrencyFromPrefs(currencyCode);
                                    editor.putBoolean("isDefaultCurrencySet", true);
                                    editor.putString("secondCurrency", currencyCode);
                                    editor.apply();
                                }
                            }
                        }

                        broadcastExchangeRateUpdate();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ZapLog.debug(LOG_TAG, "Fiat exchange rate request failed");
            }
        });

        return rateRequest;
    }


    // Event handling to notify all registered listeners to an exchange rate change.

    private void broadcastExchangeRateUpdate() {
        for (ExchangeRateListener listener : mExchangeRateListeners) {
            listener.onExchangeRatesUpdated();
        }
    }

    public void registerExchangeRateListener(ExchangeRateListener listener) {
        mExchangeRateListeners.add(listener);
    }

    public void unregisterExchangeRateListener(ExchangeRateListener listener) {
        mExchangeRateListeners.remove(listener);
    }

    public interface ExchangeRateListener {
        void onExchangeRatesUpdated();
    }

}
