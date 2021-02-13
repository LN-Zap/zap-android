package zapsolutions.zap.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zapsolutions.zap.AdvancedSettingsActivity;
import zapsolutions.zap.BuildConfig;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.pin.PinSetupActivity;
import zapsolutions.zap.util.AppUtil;
import zapsolutions.zap.util.KeystoreUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String LOG_TAG = SettingsFragment.class.getName();

    private SwitchPreference mSwHideTotalBalance;
    private ListPreference mListCurrency;
    private Preference mPinPref;
    private ListPreference mListLanguage;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);

        mPinPref = findPreference("pinPref");

        // Update our current selected first currency in the MonetaryUtil
        final ListPreference listBtcUnit = findPreference("firstCurrency");
        listBtcUnit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MonetaryUtil.getInstance().loadFirstCurrencyFromPrefs(String.valueOf(newValue));
                // Calling switch currency twice will update all currency labels across the app
                // while keeping the same currency as primary
                MonetaryUtil.getInstance().switchCurrencies();
                MonetaryUtil.getInstance().switchCurrencies();
                return true;
            }
        });


        // Action when clicked on "currency".
        // The list has to be generated on the fly based on the exchange rate data
        // we received from our provider. Therefore when the provider adds new currencies,
        // they will automatically show up in Zap.
        mListCurrency = findPreference("secondCurrency");
        mListCurrency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                createSecondCurrencyList();
                return true;
            }
        });

        // Update our current selected second currency in the MonetaryUtil
        mListCurrency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MonetaryUtil.getInstance().loadSecondCurrencyFromPrefs(String.valueOf(newValue));
                // Calling switch currency twice will update all currency labels across the app
                // while keeping the same currency as primary
                MonetaryUtil.getInstance().switchCurrencies();
                MonetaryUtil.getInstance().switchCurrencies();
                return true;
            }
        });


        // Show warning on language change as a restart is required.
        mListLanguage = findPreference("language");
        createLanguagesList();
        mListLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.settings_restartRequired)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                SharedPreferences.Editor editor = PrefsUtil.editPrefs();
                                editor.putString(PrefsUtil.LANGUAGE, newValue.toString());

                                // We have to use commit here, apply would not finish before the app is restarted.
                                editor.commit();

                                // FinishAffinity is needed here, otherwise the home activity still exist when restarting leading to lnd connection issues.
                                getActivity().finishAffinity();

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


        // Action when clicked on the pin preference
        mPinPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                    if (PrefsUtil.isPinEnabled()) {
                        Intent intent = new Intent(getActivity(), PinSetupActivity.class);
                        intent.putExtra(RefConstants.SETUP_MODE, PinSetupActivity.CHANGE_PIN);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), PinSetupActivity.class);
                        intent.putExtra(RefConstants.SETUP_MODE, PinSetupActivity.ADD_PIN);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.demo_setupWalletFirst, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });


        // Action when clicked on "reset all"
        final Preference prefResetAll = findPreference("resetAll");
        prefResetAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // We have to use commit here, apply would not finish before the app is restarted.
                PrefsUtil.editPrefs().clear().commit();
                try {
                    PrefsUtil.editEncryptedPrefs().clear().commit();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    new Cryptography(App.getAppContext()).removeKeys();
                    new KeystoreUtil().removePinActiveKey();
                } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                getActivity().finishAffinity();
                AppUtil.getInstance(getActivity()).restartApp();
                return true;
            }
        });
        // Hide development category in release build
        if (!BuildConfig.BUILD_TYPE.equals("debug")) {
            final PreferenceCategory devCategory = findPreference("devCategory");
            devCategory.setVisible(false);
        }

        // Action when clicked on "advanced settings"
        final Preference prefAdvanced = findPreference("goToAdvanced");
        prefAdvanced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AdvancedSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });


        // On hide balance option
        mSwHideTotalBalance = findPreference("hideTotalBalance");
        mSwHideTotalBalance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!mSwHideTotalBalance.isChecked()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.settings_hideTotalBalance)
                            .setMessage(R.string.settings_hideTotalBalance_explanation)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                    return true;
                } else {
                    return true;
                }
            }
        });
    }


    private CharSequence[] joinCharSequenceArrays(CharSequence[] first, CharSequence[] second) {
        if (first == null && second == null) {
            return null;
        } else if (first == null) {
            return second;
        } else if (second == null) {
            // No exchange rate has been fetched so far. This could happen if the app was started for the first time
            // without internet. Or if the user blocks connection to the exchange rate provider for example.
            return first;
        } else {
            List<CharSequence> both = new ArrayList<CharSequence>(first.length + second.length);
            Collections.addAll(both, first);
            Collections.addAll(both, second);
            return both.toArray(new CharSequence[both.size()]);
        }
    }

    private void createSecondCurrencyList() {

        CharSequence[] btcEntryValues = getActivity().getResources().getStringArray(R.array.btcUnit);
        CharSequence[] btcEntriesDisplayValue = getActivity().getResources().getStringArray(R.array.btcUnitDisplayValues);
        CharSequence[] fiatEntryValues = null;
        CharSequence[] fiatEntryDisplayValue = null;

        try {
            JSONObject jsonAvailableCurrencies = new JSONObject(PrefsUtil.getPrefs().getString(PrefsUtil.AVAILABLE_FIAT_CURRENCIES, PrefsUtil.DEFAULT_FIAT_CURRENCIES));

            JSONArray currencies = jsonAvailableCurrencies.getJSONArray("currencies");
            fiatEntryValues = new CharSequence[currencies.length()];
            fiatEntryDisplayValue = new CharSequence[currencies.length()];
            for (int i = 0, count = currencies.length(); i < count; i++) {
                try {
                    fiatEntryValues[i] = currencies.getString(i);

                    String currencyName = AppUtil.getInstance(getActivity()).getCurrencyNameFromCurrencyCode(currencies.getString(i));
                    if (currencyName == null) {
                        currencyName = currencies.getString(i);
                    } else {
                        currencyName = "(" + currencies.getString(i) + ") " + currencyName;
                    }
                    fiatEntryDisplayValue[i] = currencyName;
                } catch (JSONException e) {
                    ZapLog.d(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            ZapLog.d(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
        }

        // Combine btc list with fiat list
        CharSequence[] entryValues = joinCharSequenceArrays(btcEntryValues, fiatEntryValues);
        CharSequence[] entryDisplayValues = joinCharSequenceArrays(btcEntriesDisplayValue, fiatEntryDisplayValue);

        // Use the arrays for the list preference
        mListCurrency.setEntryValues(entryValues);
        mListCurrency.setEntries(entryDisplayValues);

    }

    private void createLanguagesList() {
        // This is necessary as we want to have "system language translatable, while the languages themselves will not be translatable
        CharSequence[] languageDisplayValues = getActivity().getResources().getStringArray(R.array.languageDisplayValues);
        languageDisplayValues[0] = getActivity().getResources().getString(R.string.settings_systemLanguage);
        mListLanguage.setEntries(languageDisplayValues);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListCurrency.setValue(PrefsUtil.getSecondCurrency());
        mListCurrency.setSummary("%s");
        createSecondCurrencyList();
        pinOptionText();
    }

    private void pinOptionText() {
        // Display add or change pin
        if (PrefsUtil.isPinEnabled()) {
            mPinPref.setTitle(R.string.settings_changePin);
        } else {
            mPinPref.setTitle(R.string.settings_addPin);
        }
    }
}
