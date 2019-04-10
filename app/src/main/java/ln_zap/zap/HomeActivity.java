package ln_zap.zap;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.preference.PreferenceManager;
import android.view.MenuItem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.connection.NetworkChangeReceiver;
import ln_zap.zap.fragments.HistoryFragment;
import ln_zap.zap.fragments.SettingsFragment;
import ln_zap.zap.fragments.WalletFragment;
import ln_zap.zap.connection.HttpClient;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.TimeOutUtil;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;

public class HomeActivity extends BaseAppCompatActivity implements LifecycleObserver {

    private static final String LOG_TAG = "Main Activity";

    private ScheduledExecutorService mExchangeRateScheduler;
    private ScheduledExecutorService mLNDInfoScheduler;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private boolean mIsExchangeRateSchedulerRunning = false;
    private boolean mIsLNDInfoSchedulerRunning = false;
    private boolean mIsNetworkChangeReceiverRunning = false;
    private Fragment mCurrentFragment = null;
    private FragmentTransaction mFt;
    private SharedPreferences mPrefs;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Fetch the transaction history
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            Wallet.getInstance().fetchLNDTransactionHistory();

            Wallet.getInstance().fetchOpenChannelsFromLND();
            Wallet.getInstance().fetchClosedChannelsFromLND();
        }
    }


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
                    mCurrentFragment = new HistoryFragment();
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
                            ZapLog.debug(LOG_TAG, "Fiat exchange rate request initiated");
                            // Adding request to request queue
                            HttpClient.getInstance().addToRequestQueue(request, "rateRequest");
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
        ZapLog.debug(LOG_TAG,"Zap moved to foreground");

        if(mPrefs.getBoolean("isWalletSetup", false) && TimeOutUtil.getInstance().isTimedOut()){
            // Go to PIN entry screen
            Intent intent = new Intent(this, PinEntryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        setupExchangeRateSchedule();
        registerNetworkStatusChangeListener();

        // Restart lnd connection
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            LndConnection.getInstance().restartBackgroundTasks();
            setupLNDInfoSchedule();

            // Fetch the transaction history
            Wallet.getInstance().fetchLNDTransactionHistory();

            Wallet.getInstance().fetchOpenChannelsFromLND();
            Wallet.getInstance().fetchClosedChannelsFromLND();
        }
    }

    // This function gets called when app is moved to background.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        ZapLog.debug(LOG_TAG,"Zap moved to background");
        stopListenersAndSchedules();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListenersAndSchedules();
    }

    private void stopListenersAndSchedules(){
        TimeOutUtil.getInstance().startTimer();

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

        if (mIsNetworkChangeReceiverRunning){
            // Kill the Network state change listener to go easy on the battery.
            unregisterReceiver(mNetworkChangeReceiver);
            mIsNetworkChangeReceiverRunning = false;
        }

        // Kill lnd connection
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            LndConnection.getInstance().stopBackgroundTasks();
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
}
