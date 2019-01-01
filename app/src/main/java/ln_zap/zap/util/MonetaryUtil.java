package ln_zap.zap.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

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



    private MonetaryUtil(Context ctx){
        mContext = ctx.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCurrentCurrency = new FiatCurrency(mPrefs.getString("currency","USD"),0);
    }

    public static MonetaryUtil getInstance(Context ctx){
        if(mInstance == null) {
            mInstance = new MonetaryUtil(ctx);
        }

        return mInstance;
    }


    public String getPrimaryDisplayAmountAndUnit(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayAmountAndUnit(value);
        }
        else{
            return "";
        }
    }

    public String getPrimaryDisplayAmount(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayAmount(value);
        }
        else{
            return getFiatDisplayAmount(value);
        }
    }

    public String getPrimaryDisplayUnit(){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getBitcoinDisplayUnit();
        }
        else{
            return getFiatDisplayUnit();
        }
    }


    public String getSecondaryDisplayAmountAndUnit(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return "";
        }
        else{
            return getBitcoinDisplayAmountAndUnit(value);
        }
    }

    public String getSecondaryDisplayAmount(long value){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getFiatDisplayAmount(value);
        }
        else{
            return getBitcoinDisplayAmount(value);
        }
    }

    public String getSecondaryDisplayUnit(){
        if(mPrefs.getBoolean("isBitcoinPrimary", true)){
            return getFiatDisplayUnit();
        }
        else{
            return getBitcoinDisplayUnit();
        }
    }

    public void loadCurrency(String currencyCode){
        try {
            JSONObject selectedCurrency = new JSONObject(mPrefs.getString("fiat_" + currencyCode, "{}"));
            FiatCurrency currency = new FiatCurrency(currencyCode, selectedCurrency.getDouble("rate"));
            mCurrentCurrency = currency;
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }


    ////////// Bitcoin display functions /////////////


    private String getBitcoinDisplayAmountAndUnit(long value){

        String selectedBTCUnit = mPrefs.getString("btcUnit","BTC");
        switch (selectedBTCUnit) {
            case "BTC":
                return formatAsBtcDisplayAmount(value) + " " + BTC_UNIT;
            case "mBTC":
                return formatAsMbtcDisplayAmount(value) + " " + MBTC_UNIT;
            case "bit":
                return formatAsBitsDisplayAmount(value) + " " + BIT_UNIT;
            case "Satoshi":
                return formatAsSatoshiDisplayAmount(value) + " " + SATOSHI_UNIT;
            default:
                return formatAsBtcDisplayAmount(value) + " " + BTC_UNIT;
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
        switch (selectedBTCUnit) {
            case "BTC":
                return BTC_UNIT;
            case "mBTC":
                return MBTC_UNIT;
            case "bit":
                return BIT_UNIT;
            case "Satoshi":
                return SATOSHI_UNIT;
            default:
                return BTC_UNIT;
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


    // request exchange rates from "blockchain.info" and save result in shared preferences.
    public void getExchangeRates() {

        // Creating request
        ZapLog.debug(LOG_TAG,"Fiat exchange rate request initiated");
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
                        editor.putString("fiat_" + fiatCode, FiatCurrency.toString());
                        availableCurrenciesArray.put(fiatCode);
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

                // ToDo: We now have the new data. Make sure everything gets updated.
                // this only loads the currency, but does not update text fields
                loadCurrency(mPrefs.getString("currency","USD"));
            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ZapLog.debug(LOG_TAG,"Fiat exchange rate request failed");
                }
        });

        // Adding request to request queue
        HttpClient.getInstance(mContext).addToRequestQueue(rateRequest, "rateRequest");
    }

}
