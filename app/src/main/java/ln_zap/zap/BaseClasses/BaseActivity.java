package ln_zap.zap.BaseClasses;

import android.app.Activity;
import android.content.Context;

import ln_zap.zap.util.LocaleUtil;

public abstract class BaseActivity extends Activity {
    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleUtil.setLocale(ctx));
    }
}
