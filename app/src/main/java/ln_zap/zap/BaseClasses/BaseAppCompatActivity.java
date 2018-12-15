package ln_zap.zap.BaseClasses;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import ln_zap.zap.util.LocaleManager;

public abstract class BaseAppCompatActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleManager.setLocale(ctx));
    }
}
