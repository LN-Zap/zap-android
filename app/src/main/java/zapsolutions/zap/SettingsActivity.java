package zapsolutions.zap;


import android.os.Bundle;

import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.fragments.SettingsFragment;


public class SettingsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContent, new SettingsFragment())
                .commit();
    }
}
