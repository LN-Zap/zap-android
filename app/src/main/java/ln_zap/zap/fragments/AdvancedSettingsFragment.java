package ln_zap.zap.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import ln_zap.zap.R;
import ln_zap.zap.interfaces.UserGuardianInterface;
import ln_zap.zap.util.UserGuardian;


public class AdvancedSettingsFragment extends PreferenceFragmentCompat implements UserGuardianInterface {

    private static final String LOG_TAG = "Advanced Settings";
    private UserGuardian mUG;
    private SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.advanced_settings, rootKey);

        mUG = new UserGuardian(getActivity(), this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


    }


    @Override
    public void guardianDialogConfirmed(String DialogName) {

    }

}
