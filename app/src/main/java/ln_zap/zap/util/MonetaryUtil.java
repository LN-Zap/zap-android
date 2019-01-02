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
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;


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
    private FiatCurrency mCurrentCurrency;



    private MonetaryUtil(){
        mContext = App.getAppContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(mPrefs.getString("fiat_" + mPrefs.getString("currency","USD"), "").equals("")){
            mCurrentCurrency = new FiatCurrency(mPrefs.getString("currency","USD"),0,0);
        }
        else{
            loadCurrencyFromPrefs(mPrefs.getString("currency","USD"));
        }
    }

    public static MonetaryUtil getInstance(){
        if(mInstance == null) {
            mInstance = new MonetaryUtil();
        }

        return mInstance;
    }


    /**
     * Get the amount and display unit of the primary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getPrimaryDisplayAmountAndUnit(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayAmountAndUnit(value);
        }
        else{
            return "";
        }
    }


    /**
     * Get the amount of the primary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getPrimaryDisplayAmount(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayAmount(value);
        }
        else{
            return getFiatDisplayAmount(value);
        }
    }


    /**
     * Get the display unit of the primary currency as properly formatted string.
     *
     * @return formatted string
     */
    public String getPrimaryDisplayUnit(){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayUnit();
        }
        else{
            return getFiatDisplayUnit();
        }
    }


    /**
     * Get the amount and display unit of the secondary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getSecondaryDisplayAmountAndUnit(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return "";
        }
        else{
            return getBitcoinDisplayAmountAndUnit(value);
        }
    }


    /**
     * Get the amount of the secondary currency as properly formatted string.
     *
     * @param value in Satoshis
     * @return formatted string
     */
    public String getSecondaryDisplayAmount(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getFiatDisplayAmount(value);
        }
        else{
            return getBitcoinDisplayAmount(value);
        }
    }


    /**
     * Get the display unit of the secondary currency as properly formatted string.
     *
     * @return formatted string
     */
    public String getSecondaryDisplayUnit(){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getFiatDisplayUnit();
        }
        else{
            return getBitcoinDisplayUnit();
        }
    }


    /**
     * This function returns how old our fiat exchange rate data is.
     *
     * @return Age in seconds.
     */
    public long getExchangeRateAge(){
        return (System.currentTimeMillis()/1000)-mCurrentCurrency.getTimestamp();
    }


    /**
     * Load a currency from the default settings using a currencyCode (USD, EUR, ...)
     * By loading it, we have access to it without parsing the JSON string over and over.
     *
     * @param currencyCode (USD, EUR, etc.)
     */
    public void loadCurrencyFromPrefs(String currencyCode){
        try {
            JSONObject selectedCurrency = new JSONObject(mPrefs.getString("fiat_" + currencyCode, "{}"));
            FiatCurrency currency = new FiatCurrency(currencyCode, selectedCurrency.getDouble("rate"), selectedCurrency.getLong("timestamp"));
            mCurrentCurrency = currency;
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }



    private void setCurrency(String currencyCode, Double rate, Long timestamp){
            FiatCurrency currency = new FiatCurrency(currencyCode, rate, timestamp);
            mCurrentCurrency = currency;
    }


    ////////// Bitcoin display functions /////////////


    private String getBitcoinDisplayAmountAndUnit(long value){

        String selectedBTCUnit = mPrefs.getString("btcUnit","BTC");
        String networkID = mPrefs.getBoolean("mainnet",true) ? "" : "t";
        switch (selectedBTCUnit) {
            case "BTC":
                return formatAsBtcDisplayAmount(value) + " " + networkID + BTC_UNIT;
            case "mBTC":
                return formatAsMbtcDisplayAmount(value) + " " + networkID + MBTC_UNIT;
            case "bit":
                return formatAsBitsDisplayAmount(value) + " " + networkID + BIT_UNIT;
            case "Satoshi":
                return formatAsSatoshiDisplayAmount(value) + " " + networkID + SATOSHI_UNIT;
            default:
                return formatAsBtcDisplayAmount(value) + " " + networkID + BTC_UNIT;
        }

    }

    private String getBitcoinDisplayAmount(long value){

        String selectedBTCUnit = mPrefs.getString("btcUnit","BTC");
        switch (selectedBTCUnit) {
            case "BTC":
                return formatAsBtcDisplayAmount(value);
            case "mBTC":
                return formatAsMbtcDisplayAmount(value);
            case "bit":
                return formatAsBitsDisplayAmount(value);
            case "Satoshi":
                return formatAsSatoshiDisplayAmount(value);
            default:
                return formatAsBtcDisplayAmount(value);
        }

    }

    private String getBitcoinDisplayUnit(){

        String selectedBTCUnit = mPrefs.getString("btcUnit","BTC");
        String networkID = mPrefs.getBoolean("mainnet",true) ? "" : "t";
        switch (selectedBTCUnit) {
            case "BTC":
                return networkID + BTC_UNIT;
            case "mBTC":
                return networkID + MBTC_UNIT;
            case "bit":
                return networkID + BIT_UNIT;
            case "Satoshi":
                return networkID + SATOSHI_UNIT;
            default:
                return networkID + BTC_UNIT;
        }

    }

    private String formatAsBtcDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(8);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        return df.format(value /1e8);
    }

    private String formatAsMbtcDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(5);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(19);
        return df.format(value /100000d);
    }

    private String formatAsBitsDisplayAmount(long value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(22);
        String result= df.format(value /100d);

        // If we have a fraction, then always show 2 fraction digits for bits
        if (result.contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))){
            df.setMinimumFractionDigits(2);
            return df.format(value /100d);
        }
        else {
            return result;
        }
    }

    private String formatAsSatoshiDisplayAmount(long value) {
        // Satoshis are the smallest unit, there are no fractions.
        // To avoid a value being recognized as a fraction, we use spaces as group separators.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#", symbols);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        return df.format(value);
    }


    ////////// Fiat functions /////////////

    private String getFiatDisplayAmount(long value){
        double fiatValue = (mCurrentCurrency.getRate() / 1e8)*value;
        return formatAsFiatDisplayAmount(fiatValue);
    }

    private String getFiatDisplayUnit(){
        return mCurrentCurrency.getCode();
    }

    private String formatAsFiatDisplayAmount(double value) {
        Locale loc = mContext.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(22);
        String result= df.format(value);

        // If we have a fraction, then always show 2 fraction digits for fiat
        if (result.contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))){
            df.setMinimumFractionDigits(2);
            return df.format(value);
        }
        else {
            return result;
        }
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
                        FiatCurrency.put("rate",ReceivedCurrency.getDouble("15m"));
                        FiatCurrency.put("symbol", ReceivedCurrency.getString("symbol"));
                        FiatCurrency.put("timestamp",System.currentTimeMillis()/1000);
                        editor.putString("fiat_" + fiatCode, FiatCurrency.toString());
                        availableCurrenciesArray.put(fiatCode);
                        // Update the current fiat currency of the Monetary util
                        if (fiatCode.equals(mPrefs.getString("currency","USD"))){
                            setCurrency(fiatCode,ReceivedCurrency.getDouble("15m"),System.currentTimeMillis()/1000);
                        }
                    } catch (JSONException e) {
                        ZapLog.debug(LOG_TAG,"Unable to decode currency from fiat exchange rate request");
                    }
                }
                try {
                    availableCurrencies.put("currencies", availableCurrenciesArray);
                    editor.putString("fiat_available", availableCurrencies.toString());
                } catch(JSONException e) {
                    ZapLog.debug(LOG_TAG,"unable to add array to object");
                }
                editor.apply();

            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ZapLog.debug(LOG_TAG,"Fiat exchange rate request failed");
                }
        });

        return rateRequest;
    }

}
