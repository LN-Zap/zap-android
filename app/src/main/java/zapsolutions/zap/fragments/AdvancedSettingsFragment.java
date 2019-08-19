package zapsolutions.zap.fragments;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import zapsolutions.zap.R;
import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.util.UserGuardian;


public class AdvancedSettingsFragment extends PreferenceFragmentCompat implements UserGuardianInterface {

    private static final String LOG_TAG = AdvancedSettingsFragment.class.getName();
    private UserGuardian mUG;
    private SwitchPreference mSwScrambledPin;
    private SwitchPreference mSwScreenProtection;
    private ListPreference mSwBlockExplorer;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.advanced_settings, rootKey);

        mUG = new UserGuardian(getActivity(), this);

        // On change block explorer option
        mSwBlockExplorer = findPreference("blockExplorer");
        mSwBlockExplorer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null && newValue.toString().equalsIgnoreCase("Blockstream (v3 Tor)")) {
                    Toast.makeText(getActivity(), R.string.settings_blockExplorer_tor_toast, Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    return true;
                }
            }
        });

        // On change scramble pin option
        mSwScrambledPin = findPreference("scramblePin");
        mSwScrambledPin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mSwScrambledPin.isChecked()) {
                    mUG.securityScrambledPin();
                    // the value is set from the guardian callback, that's why we don't change switch state here.
                    return false;
                } else {
                    return true;
                }
            }
        });

        // On change screen recording option
        mSwScreenProtection = findPreference("preventScreenRecording");
        mSwScreenProtection.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mSwScreenProtection.isChecked()) {
                    mUG.securityScreenProtection();
                    // the value is set from the guardian callback, that's why we don't change switch state here.
                    return false;
                } else {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    return true;
                }
            }
        });

    }


    @Override
    public void guardianDialogConfirmed(String DialogName) {
        switch (DialogName) {
            case UserGuardian.DISABLE_SCRAMBLED_PIN:
                mSwScrambledPin.setChecked(false);
                break;
            case UserGuardian.DISABLE_SCREEN_PROTECTION:
                mSwScreenProtection.setChecked(false);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                break;
        }
    }

}
