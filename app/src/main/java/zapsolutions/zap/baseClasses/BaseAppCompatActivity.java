package zapsolutions.zap.baseClasses;

import android.content.Context;
import android.view.MenuItem;
import android.view.WindowManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import zapsolutions.zap.util.LocaleUtil;
import zapsolutions.zap.util.PrefsUtil;

public abstract class BaseAppCompatActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context ctx) {
        // Set the correct locale
        super.attachBaseContext(LocaleUtil.setLocale(ctx));


    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeScreenRecordingSecurity();

        // Enable back button if an action bar is supported by the theme
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Secure against screenshots and automated screen recording.
     * Keep in mind that this does not prevent popups and other
     * dialogs to be secured as well. Extra security measures have to be considered.
     * Check out the following link for more details:
     * https://github.com/commonsguy/cwac-security/blob/master/docs/FLAGSECURE.md
     */
    private void initializeScreenRecordingSecurity() {
        if (PrefsUtil.preventScreenRecording()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // Go back if back button was pressed on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
