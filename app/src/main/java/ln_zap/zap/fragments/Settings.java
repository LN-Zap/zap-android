package ln_zap.zap.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import ln_zap.zap.interfaces.UserGuardianInterface;
import ln_zap.zap.R;
import ln_zap.zap.util.AppUtil;

import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.UserGuardian;
import ln_zap.zap.util.ZapLog;


public class Settings extends PreferenceFragmentCompat implements UserGuardianInterface {

    private static final String LOG_TAG = "Settings";


    private UserGuardian mUG;
    private SwitchPreference mSwScreenProtection;
    private SwitchPreference mSwScrambledPin;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);

        mUG = new UserGuardian(getActivity(),this);

        // Action when clicked on "currency". The list has to be generated based on the exchange rate
        // data we received from our provider. Therefore when the provider adds new currencies,
        // they will automatically show up in Zap.
        final ListPreference listCurrency = findPreference("currency");
        listCurrency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                CharSequence[] entries = null;

                try {
                    JSONObject jsonAvailableCurrencies = new JSONObject(prefs.getString("fiat_available", "[]"));

                    JSONArray currencies = jsonAvailableCurrencies.getJSONArray("currencies");
                    entries = new CharSequence[currencies.length()];

                    for (int i = 0, count = currencies.length(); i < count; i++) {
                        try {
                            entries[i] = currencies.getString(i);
                        } catch (JSONException e) {
                            ZapLog.debug(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
                        }
                    }

                } catch (JSONException e) {
                    ZapLog.debug(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
                }

                listCurrency.setEntries(entries);
                listCurrency.setEntryValues(entries);

                return true;
            }
        });

        // Update our current selected currency in the MonetaryUtil
        listCurrency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MonetaryUtil.getInstance(getActivity()).loadCurrency(String.valueOf(newValue));
                return true;
            }
        });


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

        // On change screen recording option
        mSwScreenProtection = (SwitchPreference) findPreference("preventScreenRecording");
        mSwScreenProtection.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mSwScreenProtection.isChecked()){
                    mUG.securityScreenProtection();
                    // the value is set from the guardian callback, that's why we don't chang switch state here.
                    return false;
                }
                else{
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    return true;
                }
            }
        });

        // On change scramble pin option
        mSwScrambledPin = (SwitchPreference) findPreference("scramblePin");
        mSwScrambledPin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mSwScrambledPin.isChecked()){
                    mUG.securityScrambledPin();
                    // the value is set from the guardian callback, that's why we don't chang switch state here.
                    return false;
                }
                else{
                    return true;
                }
            }
        });

        // Action when clicked on "reset security warnings"
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
                mSwScrambledPin.setChecked(false);
                break;
            case UserGuardian.DISABLE_SCREEN_PROTECTION:
                mSwScreenProtection.setChecked(false);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                break;
        }
    }

}
