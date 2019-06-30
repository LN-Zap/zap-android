package zapsolutions.zap.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import zapsolutions.zap.AdvancedSettingsActivity;
import zapsolutions.zap.BuildConfig;
import zapsolutions.zap.channelManagement.ManageChannelsActivity;
import zapsolutions.zap.setup.SetupActivity;
import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.R;
import zapsolutions.zap.util.AppUtil;

import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class SettingsFragment extends PreferenceFragmentCompat{

    private static final String LOG_TAG = "Settings";

    private SwitchPreference mSwHideTotalBalance;
    private ListPreference mListCurrency;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);

        // Update our current selected first currency in the MonetaryUtil
        final ListPreference listBtcUnit = findPreference("firstCurrency");
        listBtcUnit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MonetaryUtil.getInstance().loadFirstCurrencyFromPrefs(String.valueOf(newValue));
                return true;
            }
        });


        // Action when clicked on "currency".
        // The list has to be generated on the fly based on the exchange rate data
        // we received from our provider. Therefore when the provider adds new currencies,
        // they will automatically show up in Zap.
        mListCurrency = findPreference("secondCurrency");
        createSecondCurrencyList();
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
                                SharedPreferences.Editor editor = PrefsUtil.edit();

                                if (listLanguage.getValue().equals("system")) {
                                    editor.putString("language", "english");
                                } else {
                                    editor.putString("language", "system");
                                }
                                // We have to use commit here, apply would not finish before the app is restarted.
                                editor.commit();
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

        /*
        // Action when clicked on "Manage wallets"
        final Preference prefManageWallets = findPreference("manageWallets");
        prefManageWallets.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ManageWalletsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        */

        // Action when clicked on "Manage channels"
        final Preference prefManageChannels = findPreference("manageLightningChannels");
        prefManageChannels.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ManageChannelsActivity.class);
                startActivity(intent);
                return true;
            }
        });



        // Action when clicked on "reset connection settings"
        final Preference prefResetConfig = findPreference("resetConfig");
        prefResetConfig.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PrefsUtil.isWalletSetup()){
                    Intent intent = new Intent(getActivity(), SetupActivity.class);
                    intent.putExtra("setupMode", SetupActivity.CHANGE_CONNECTION);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), R.string.demo_setupWalletFirst,Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });


        // Action when clicked on "change pin"
        final Preference prefChangePin = findPreference("changePIN");
        prefChangePin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (PrefsUtil.isWalletSetup()) {
                    Intent intent = new Intent(getActivity(), SetupActivity.class);
                    intent.putExtra("setupMode", SetupActivity.CHANGE_PIN);
                    startActivity(intent);
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
                PrefsUtil.edit().clear().commit();
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


        // Action when clicked on "reset security warnings"
        final Preference prefResetGuardian = findPreference("resetGuardian");
        prefResetGuardian.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UserGuardian.reenableAllSecurityWarnings(getActivity());
                Toast.makeText(getActivity(), R.string.guardian_reset, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // Action when clicked on "need help"
        final Preference prefHelp = findPreference("help");
        prefHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://ln-zap.github.io/zap-tutorials/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getActivity().startActivity(browserIntent);
                return true;
            }
        });

        // Action when clicked on "reportBug"
        final Preference prefIssue = findPreference("reportIssue");
        prefIssue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://github.com/LN-Zap/zap-android/issues";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getActivity().startActivity(browserIntent);
                return true;
            }
        });


        // Action when clicked on "About"
        final Preference prefAbout = findPreference("about");
        prefAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_about)
                        .setMessage("Version:  " + BuildConfig.VERSION_NAME +
                                "\nBuild:  " + BuildConfig.VERSION_CODE +
                                "\nLND version:  " + Wallet.getInstance().getLNDVersion().split(" commit")[0])
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                Dialog dlg = adb.create();
                // Apply FLAG_SECURE to dialog to prevent screen recording
                if (PrefsUtil.preventScreenRecording()) {
                    dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                dlg.show();
                return true;
            }
        });
    }


    private CharSequence[] joinCharSequenceArrays(CharSequence[] first, CharSequence[] second) {
        List<CharSequence> both = new ArrayList<CharSequence>(first.length + second.length);
        Collections.addAll(both, first);
        Collections.addAll(both, second);
        return both.toArray(new CharSequence[both.size()]);
    }

    private void createSecondCurrencyList() {

        CharSequence[] btcEntryValues = getActivity().getResources().getStringArray(R.array.btcUnit);
        CharSequence[] btcEntriesDisplayValue = getActivity().getResources().getStringArray(R.array.btcUnitDisplayValues);
        CharSequence[] fiatEntryValues = null;
        CharSequence[] fiatEntryDisplayValue = null;

        try {
            JSONObject jsonAvailableCurrencies = new JSONObject(PrefsUtil.getPrefs().getString("fiat_available", "[]"));

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
                    ZapLog.debug(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            ZapLog.debug(LOG_TAG, "Error reading JSON from Preferences: " + e.getMessage());
        }

        // Combine btc list with fiat list
        CharSequence[] entryValues = joinCharSequenceArrays(btcEntryValues, fiatEntryValues);
        CharSequence[] entryDisplayValues = joinCharSequenceArrays(btcEntriesDisplayValue, fiatEntryDisplayValue);

        // Use the arrays for the list preference
        mListCurrency.setEntryValues(entryValues);
        mListCurrency.setEntries(entryDisplayValues);

    }


}
