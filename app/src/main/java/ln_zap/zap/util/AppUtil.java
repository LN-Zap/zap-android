package ln_zap.zap.util;

import android.content.Context;
import android.content.res.Resources;

import com.jakewharton.processphoenix.ProcessPhoenix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import ln_zap.zap.R;


public class AppUtil {

    private static final String LOG_TAG = "AppUtil";

	private static AppUtil mInstance = null;
	private static Context mContext = null;



    private AppUtil() { ; }

	public static AppUtil getInstance(Context ctx) {
		
		mContext = ctx;
		
		if(mInstance == null) {
			mInstance = new AppUtil();
		}
		
		return mInstance;
	}


	/**
	 * Use this function to load a JSON file from res/raw folder.
	 * @param id resource id
	 * @return The JSON file as string.
	 */
	public String loadJSONFromResource(int id) {
		String json = null;
		try {

			InputStream inputStream = mContext.getResources().openRawResource(id);
			int size = inputStream.available();
			byte[] buffer = new byte[size];
			inputStream.read(buffer);
			inputStream.close();
			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

    /**
     * This function will return a currency code (3 Letters) that corresponds to the locale of the
     * system.
     * @return
     */
	public String getSystemCurrencyCode(){
        String currencyCode = null;

	    String countries = loadJSONFromResource(R.raw.country_list);

        // Get the country code from system settings
        String country = Resources.getSystem().getConfiguration().locale.getISO3Country();

        // Find the corresponding currency code
        try {
            JSONObject jsonCountryList = new JSONObject(countries);
            if (jsonCountryList.has(country)) {
                currencyCode = jsonCountryList.getJSONObject(country).getString("CurrencyCode");
            }
        } catch (JSONException e) {
            ZapLog.debug(LOG_TAG, "Error reading country_list JSON: " + e.getMessage());
        }
        return currencyCode;
    }


	public void restartApp() {
		ProcessPhoenix.triggerRebirth(mContext);
	}

}
