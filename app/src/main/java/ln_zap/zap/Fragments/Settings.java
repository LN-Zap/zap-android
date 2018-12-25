package ln_zap.zap.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import ln_zap.zap.Interfaces.UserGuardianInterface;
import ln_zap.zap.R;
import ln_zap.zap.util.AppUtil;
import ln_zap.zap.util.UserGuardian;


public class Settings extends PreferenceFragmentCompat implements UserGuardianInterface {

    private UserGuardian UG;
    private SwitchPreference swScreenProtection;
    private SwitchPreference swScrambledPin;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);

        UG = new UserGuardian(getActivity(),this);

        // Show warning on language change as a restart is required.
        final ListPreference listLanguage = (ListPreference) findPreference("language");
        listLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.settings_restartRequired)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {


                                if (listLanguage.getValue().equals("system")) {
                                    listLanguage.setValue("english");
                                } else {
                                    listLanguage.setValue("system");
                                }
                                AppUtil.getInstance(getActivity()).restartApp();

                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ;
                    }
                }).show();

                return false;
            }
        });

        // change screen recording option
        swScreenProtection = (SwitchPreference) findPreference("preventScreenRecording");
        swScreenProtection.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (swScreenProtection.isChecked()){
                    UG.securityScreenProtection();
                    // the value is set from the guardian callback, that's why we don't chang switch state here.
                    return false;
                }
                else{
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    return true;
                }
            }
        });

        // change screen recording option
        swScrambledPin = (SwitchPreference) findPreference("scramblePin");
        swScrambledPin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (swScrambledPin.isChecked()){
                    UG.securityScrambledPin();
                    // the value is set from the guardian callback, that's why we don't chang switch state here.
                    return false;
                }
                else{
                    return true;
                }
            }
        });

        final Preference prefResetGuardian = findPreference("resetGuardian");
        prefResetGuardian.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UserGuardian.reenableAllSecurityWarnings(getActivity());
                Toast.makeText(getActivity(),R.string.guardian_reset,Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }


    @Override
    public void guardianDialogConfirmed(String DialogName) {
        switch (DialogName) {
            case UserGuardian.DISABLE_SCRAMBLED_PIN:
                swScrambledPin.setChecked(false);
                break;
            case UserGuardian.DISABLE_SCREEN_PROTECTION:
                swScreenProtection.setChecked(false);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                break;
        }
    }
}
