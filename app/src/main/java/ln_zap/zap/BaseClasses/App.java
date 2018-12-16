package ln_zap.zap.BaseClasses;

import android.app.Application;
import android.content.Context;

import ln_zap.zap.util.LocaleUtil;


// This class is used as Application class for Zap.

public class App extends Application {

    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleUtil.setLocale(ctx));
    }

}