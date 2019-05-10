package ln_zap.zap.baseClasses;

import android.app.Application;
import android.content.Context;

import ln_zap.zap.util.LocaleUtil;


// This class is used as Application class for Zap.

public class App extends Application {
    public App() {
        mContext = this;
    }

    private static App mContext;


    // keeping pin user entered in memory in order to encrypt other preferences/values
    public String inMemoryPin;
    // temporary variable when pin confirmation is needed
    public String pinTemp;

    // keep the data from the URI Scheme in memory, so we can access it from anywhere.
    private String uriSchemeData;


    public boolean connectionToLNDEstablished = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleUtil.setLocale(ctx));
    }

    public static App getAppContext() {
        return mContext;
    }

    public String getUriSchemeData() {
        return uriSchemeData;
    }

    public void setUriSchemeData(String data) {
        uriSchemeData = data;
    }
}