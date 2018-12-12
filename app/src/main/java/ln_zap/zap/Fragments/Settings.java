package ln_zap.zap.Fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import ln_zap.zap.R;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
