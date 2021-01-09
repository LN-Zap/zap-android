package zapsolutions.zap.fragments;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import zapsolutions.zap.R;
import zapsolutions.zap.util.BiometricUtil;
import zapsolutions.zap.util.ExchangeRateUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.UserGuardian;


public class AdvancedSettingsFragment extends PreferenceFragmentCompat {

    private static final String LOG_TAG = AdvancedSettingsFragment.class.getName();
    private SwitchPreference mSwScrambledPin;
    private SwitchPreference mSwScreenProtection;
    private ListPreference mListBlockExplorer;
    private ListPreference mListLnExpiry;
    private ListPreference mListFeeLimit;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.advanced_settings, rootKey);

        // On change block explorer option
        mListBlockExplorer = findPreference("blockExplorer");
        mListBlockExplorer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null && newValue.toString().equalsIgnoreCase("Blockstream (v3 Tor)")) {
                    Toast.makeText(getActivity(), R.string.settings_blockExplorer_tor_toast, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        // Request exchange rates when the provider changed
        ListPreference listExchangeRateProvider = findPreference("exchangeRateProvider");
        listExchangeRateProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PrefsUtil.editPrefs().putString(PrefsUtil.EXCHANGE_RATE_PROVIDER, newValue.toString()).commit();
                ExchangeRateUtil.getInstance().getExchangeRates();
                return true;
            }
        });

        // Create invoice expiry display entries. For the sake of plurals this has to be done by code.
        mListLnExpiry = findPreference("lightning_expiry");
        createLnExpiryDisplayEntries();

        mListFeeLimit = findPreference("lightning_feeLimit");
        mListFeeLimit.setOnPreferenceChangeListener((preference, newValue) -> {
            setFeeSummary(preference, newValue.toString());
            return true;
        });

        setFeeSummary(mListFeeLimit, mListFeeLimit.getValue());

        // Remove Biometrics setting if it is not available anyway on the device.
        SwitchPreference swBiometrics = findPreference("biometricsEnabled");
        if (!BiometricUtil.hardwareAvailable()) {
            swBiometrics.setVisible(false);
        }

        // On change scramble pin option
        mSwScrambledPin = findPreference("scramblePin");
        mSwScrambledPin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mSwScrambledPin.isChecked()) {
                    // Ask user to confirm disabling scramble
                    new UserGuardian(getActivity(), () -> mSwScrambledPin.setChecked(false)).securityScrambledPin();
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
                    // Ask user to confirm disabling screen protection
                    new UserGuardian(getActivity(), () -> {
                        mSwScreenProtection.setChecked(false);
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }).securityScreenProtection();
                    // the value is set from the guardian callback, that's why we don't change switch state here.
                    return false;
                } else {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    return true;
                }
            }
        });

        // Action when clicked on "reset security warnings"
        final Preference prefResetGuardian = findPreference("resetGuardian");
        prefResetGuardian.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UserGuardian.reenableAllSecurityWarnings();
                Toast.makeText(getActivity(), R.string.guardian_reset, Toast.LENGTH_LONG).show();
                return true;
            }
        });

    }

    private void setFeeSummary(Preference preference, String value) {
        String s = value.replace("%", "%%");
        String string = getString(R.string.fee_limit_threshold, RefConstants.LN_PAYMENT_FEE_THRESHOLD, s);
        preference.setSummary(string);
    }

    private void createLnExpiryDisplayEntries() {
        CharSequence[] lnExpiryDisplayEntries = new CharSequence[9];
        lnExpiryDisplayEntries[0] = getActivity().getResources().getQuantityString(R.plurals.duration_minute, 1, 1);
        lnExpiryDisplayEntries[1] = getActivity().getResources().getQuantityString(R.plurals.duration_minute, 10, 10);
        lnExpiryDisplayEntries[2] = getActivity().getResources().getQuantityString(R.plurals.duration_minute, 30, 30);
        lnExpiryDisplayEntries[3] = getActivity().getResources().getQuantityString(R.plurals.duration_hour, 1, 1);
        lnExpiryDisplayEntries[4] = getActivity().getResources().getQuantityString(R.plurals.duration_hour, 6, 6);
        lnExpiryDisplayEntries[5] = getActivity().getResources().getQuantityString(R.plurals.duration_day, 1, 1);
        lnExpiryDisplayEntries[6] = getActivity().getResources().getQuantityString(R.plurals.duration_week, 1, 1);
        lnExpiryDisplayEntries[7] = getActivity().getResources().getQuantityString(R.plurals.duration_month, 1, 1);
        lnExpiryDisplayEntries[8] = getActivity().getResources().getQuantityString(R.plurals.duration_year, 1, 1);

        mListLnExpiry.setEntries(lnExpiryDisplayEntries);
    }
}
