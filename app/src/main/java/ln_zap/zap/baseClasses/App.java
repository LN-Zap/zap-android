package ln_zap.zap.baseClasses;

import android.app.Application;
import android.content.Context;

import ln_zap.zap.util.LocaleUtil;


// This class is used as Application class for Zap.

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        App.mContext = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleUtil.setLocale(ctx));
    }

    public static Context getAppContext() {
        return App.mContext;
    }
}