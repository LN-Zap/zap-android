package zapsolutions.zap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.connection.internetConnectionStatus.NetworkChangeReceiver;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.fragments.SettingsFragment;
import zapsolutions.zap.fragments.WalletFragment;
import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.pin.PinEntryActivity;
import zapsolutions.zap.transactionHistory.TransactionHistoryFragment;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.TorUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class HomeActivity extends BaseAppCompatActivity implements LifecycleObserver,
        SharedPreferences.OnSharedPreferenceChangeListener,
        Wallet.InfoListener, Wallet.WalletLoadedListener, UserGuardianInterface {

    private static final String LOG_TAG = HomeActivity.class.getName();
    private Handler mHandler;
    private UserGuardian mUG;
    private InputMethodManager mInputMethodManager;
    private ScheduledExecutorService mExchangeRateScheduler;
    private ScheduledExecutorService mLNDInfoScheduler;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private boolean mIsExchangeRateSchedulerRunning = false;
    private boolean mIsLNDInfoSchedulerRunning = false;
    private boolean mIsNetworkChangeReceiverRunning = false;
    private Fragment mCurrentFragment = null;
    private FragmentTransaction mFt;
    private boolean mInfoChangeListenerRegistered;
    private boolean mWalletLoadedListenerRegistered;
    private boolean mMainnetWarningShownOnce;
    private boolean mIsFirstUnlockAttempt = true;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    // Display the fragment as main content.
                    mFt = getSupportFragmentManager().beginTransaction();
                    mCurrentFragment = new WalletFragment();
                    mFt.replace(R.id.mainContent, mCurrentFragment);
                    //mFt.addToBackStack(null);
                    mFt.commit();
                    return true;
                case R.id.navigation_history:
                    // Display the fragment as main content.
                    mFt = getSupportFragmentManager().beginTransaction();
                    mCurrentFragment = new TransactionHistoryFragment();
                    mFt.replace(R.id.mainContent, mCurrentFragment);
                    //mFt.addToBackStack(null);
                    mFt.commit();
                    return true;
                case R.id.navigation_settings:
                    // Display the fragment as main content.
                    mFt = getSupportFragmentManager().beginTransaction();
                    mCurrentFragment = new SettingsFragment();
                    mFt.replace(R.id.mainContent, mCurrentFragment);
                    //mFt.addToBackStack(null);
                    mFt.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUG = new UserGuardian(this, this);
        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mHandler = new Handler();

        // Register observer to detect if app goes to background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // Set wallet fragment as beginning fragment
        mFt = getSupportFragmentManager().beginTransaction();
        mCurrentFragment = new WalletFragment();
        mFt.replace(R.id.mainContent, mCurrentFragment);
        mFt.commit();

        // Setup Listener
        BottomNavigationView navigation = findViewById(R.id.mainNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    // This schedule keeps us up to date on exchange rates
    private void setupExchangeRateSchedule() {

        if (!mIsExchangeRateSchedulerRunning) {
            mIsExchangeRateSchedulerRunning = true;
            final JsonObjectRequest request = MonetaryUtil.getInstance().getExchangeRates();

            mExchangeRateScheduler =
                    Executors.newSingleThreadScheduledExecutor();

            mExchangeRateScheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            if (!MonetaryUtil.getInstance().getSecondCurrency().isBitcoin() ||
                                    !PrefsUtil.getPrefs().contains(PrefsUtil.AVAILABLE_FIAT_CURRENCIES)) {
                                ZapLog.debug(LOG_TAG, "Fiat exchange rate request initiated");
                                // Adding request to request queue
                                HttpClient.getInstance().addToRequestQueue(request, "rateRequest");
                            }
                        }
                    }, 0, 3, TimeUnit.MINUTES);
        }

    }

    // This scheduled LND info request lets us know
    // if we have a working connection to LND and if we are still in sync with the network
    private void setupLNDInfoSchedule() {

        if (!mIsLNDInfoSchedulerRunning) {
            mIsLNDInfoSchedulerRunning = true;
            mLNDInfoScheduler =
                    Executors.newSingleThreadScheduledExecutor();

            mLNDInfoScheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            ZapLog.debug(LOG_TAG, "LND info check initiated");
                            Wallet.getInstance().fetchInfoFromLND();
                        }
                    }, 0, 30, TimeUnit.SECONDS);
        }

    }

    // Register the network status changed listener to handle network changes
    private void registerNetworkStatusChangeListener() {

        if (!mIsNetworkChangeReceiverRunning) {
            mIsNetworkChangeReceiverRunning = true;
            mNetworkChangeReceiver = new NetworkChangeReceiver();
            IntentFilter networkStatusIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mNetworkChangeReceiver, networkStatusIntentFilter);
        }

    }

    // This function gets called when app is moved to foreground.
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        ZapLog.debug(LOG_TAG, "Zap moved to foreground");

        // Test if Lockscreen should be shown.
        if (PrefsUtil.isWalletSetup() && TimeOutUtil.getInstance().isTimedOut()) {
            if (PrefsUtil.isPinEnabled()) {
                // Go to PIN entry screen
                Intent intent = new Intent(this, PinEntryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                // Check if pin is active according to key store
                boolean isPinActive = false;
                try {
                    isPinActive =  new Cryptography(this).isPinActive();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isPinActive){
                    // According to the key store, the pin is still active. This happens if the pin got deleted from the prefs without also removing the keystore entry.
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.error_pin_deactivation_attempt)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    HomeActivity.this.finish();
                                }
                            }).show();
                } else {
                    continueMoveToForeground();
                }
            }
        } else {
            continueMoveToForeground();
        }
    }

    private void continueMoveToForeground(){
        // start listeners and schedules
        setupExchangeRateSchedule();
        registerNetworkStatusChangeListener();

        if (!mWalletLoadedListenerRegistered) {
            Wallet.getInstance().registerWalletLoadedListener(this);
            mWalletLoadedListenerRegistered = true;
        }

        if (!mInfoChangeListenerRegistered) {
            Wallet.getInstance().registerInfoListener(this);
            mInfoChangeListenerRegistered = true;
        }

        PrefsUtil.getPrefs().registerOnSharedPreferenceChangeListener(this);

        // Start lnd connection
        if (PrefsUtil.isWalletSetup()) {
            TimeOutUtil.getInstance().setCanBeRestarted(true);

            LndConnection.getInstance().openConnection();

            if (TorUtil.isCurrentConnectionTor() && !TorUtil.isOrbotInstalled(this)) {
                TorUtil.askToInstallOrbotIfMissing(this);
            } else {
                Wallet.getInstance().checkIfLndIsReachableAndTriggerWalletLoadedInterface();
            }
        }
    }

    // This function gets called when app is moved to background.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {

        ZapLog.debug(LOG_TAG, "Zap moved to background");

        App.getAppContext().connectionToLNDEstablished = false;

        stopListenersAndSchedules();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListenersAndSchedules();
        // Remove observer to detect if app goes to background
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

    private void stopListenersAndSchedules() {
        if (TimeOutUtil.getInstance().getCanBeRestarted()) {
            TimeOutUtil.getInstance().restartTimer();
            ZapLog.debug(LOG_TAG, "PIN timer restarted");
        }
        TimeOutUtil.getInstance().setCanBeRestarted(false);

        // Unregister Handler, Wallet Loaded & Info Listener
        Wallet.getInstance().unregisterWalletLoadedListener(this);
        mWalletLoadedListenerRegistered = false;
        Wallet.getInstance().unregisterInfoListener(this);
        mInfoChangeListenerRegistered = false;
        mHandler.removeCallbacksAndMessages(null);

        PrefsUtil.getPrefs().unregisterOnSharedPreferenceChangeListener(this);

        if (mIsExchangeRateSchedulerRunning) {
            // Kill the scheduled exchange rate requests to go easy on the battery.
            mExchangeRateScheduler.shutdownNow();
            mIsExchangeRateSchedulerRunning = false;
        }

        if (mIsLNDInfoSchedulerRunning) {
            // Kill the LND info requests to go easy on the battery.
            mLNDInfoScheduler.shutdownNow();
            mIsLNDInfoSchedulerRunning = false;
        }

        if (mIsNetworkChangeReceiverRunning) {
            // Kill the Network state change listener to go easy on the battery.
            unregisterReceiver(mNetworkChangeReceiver);
            mIsNetworkChangeReceiverRunning = false;
        }

        // Kill Server Streams
        Wallet.getInstance().cancelSubscriptions();

        // Kill lnd connection
        if (PrefsUtil.isWalletSetup()) {
            LndConnection.getInstance().closeConnection();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirmExit)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        HomeActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onInfoUpdated(boolean connected) {
        if ((PrefsUtil.isWalletSetup())) {
            if (!Wallet.getInstance().isTestnet() && Wallet.getInstance().isConnectedToLND()) {
                if (!mMainnetWarningShownOnce) {
                    // Show mainnet not ready warning
                    mUG.securityMainnetNotReady();
                    mMainnetWarningShownOnce = true;
                }
            }
        }
    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {

    }

    @Override
    public void onWalletLoadedUpdated(boolean success, int error) {
        if (success) {
            // We managed to establish a connection to LND.
            // Now we can start to fetch all information needed from LND
            App.getAppContext().connectionToLNDEstablished = true;

            setupLNDInfoSchedule();

            // Fetch the transaction history
            Wallet.getInstance().fetchLNDTransactionHistory();

            Wallet.getInstance().fetchChannelsFromLND();

            Wallet.getInstance().subscribeToTransactions();
            Wallet.getInstance().subscribeToInvoices();

            if (mHandler != null) {
                mHandler.postDelayed(() -> Wallet.getInstance().subscribeToChannelEvents(), 3000);
            }

            ZapLog.debug(LOG_TAG, "Wallet loaded");
        } else {
            if (error == Wallet.WalletLoadedListener.ERROR_LOCKED) {


                // Show unlock dialog
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.unlock_wallet);
                adb.setCancelable(false);
                View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input_password, null, false);

                final EditText input = viewInflated.findViewById(R.id.input);
                input.setShowSoftInputOnFocus(true);
                input.requestFocus();

                mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                adb.setView(viewInflated);

                adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((WalletFragment) mCurrentFragment).showLoadingForWalletUnlock();
                        Wallet.getInstance().unlockWallet(input.getText().toString());
                        mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        mIsFirstUnlockAttempt = false;
                        dialog.dismiss();
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager inputMethodManager = (InputMethodManager) HomeActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        ((WalletFragment) mCurrentFragment).showErrorAfterNotUnlocked();
                        mIsFirstUnlockAttempt = true;
                        dialog.cancel();
                    }
                });

                adb.show();
                ((WalletFragment) mCurrentFragment).showBackgroundForWalletUnlock();

                if (!mIsFirstUnlockAttempt) {
                    Toast.makeText(HomeActivity.this, R.string.error_wrong_password, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update if primary currency has been switched from this or another activity
        if (key.equals(PrefsUtil.PREVENT_SCREEN_RECORDING)) {
            if (PrefsUtil.preventScreenRecording()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }
}
