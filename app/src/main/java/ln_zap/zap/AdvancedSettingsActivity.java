package ln_zap.zap;


import android.os.Bundle;

import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.fragments.AdvancedSettingsFragment;


public class AdvancedSettingsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContent, new AdvancedSettingsFragment())
                .commit();
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    /*
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }
    */
}
