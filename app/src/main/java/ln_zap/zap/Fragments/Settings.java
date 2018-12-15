package ln_zap.zap.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import ln_zap.zap.R;
import ln_zap.zap.util.AppUtil;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);


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

    }
}
