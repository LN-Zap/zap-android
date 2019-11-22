package zapsolutions.zap.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.preference.PreferenceManager;

import java.util.Locale;


public class LocaleUtil {


    public static Context setLocale(Context ctx) {
        return updateResources(ctx, getLanguageCode(ctx));
    }

    // Get selected language code
    private static String getLanguageCode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.getString("language", "system").equals("system")) {
            return Resources.getSystem().getConfiguration().locale.getLanguage();
        }
		if (prefs.getString("language", "system").equals("bulgarian")) return "bg";
		else {
            return "en";
        }
    }

    // Create and return a new context, based on the current context updated with the desired locale
    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
        return context;
    }

}
