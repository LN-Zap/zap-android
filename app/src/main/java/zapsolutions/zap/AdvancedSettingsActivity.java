package zapsolutions.zap;


import android.os.Bundle;

import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.fragments.AdvancedSettingsFragment;


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
}
