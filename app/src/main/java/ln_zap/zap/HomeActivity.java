package ln_zap.zap;

import androidx.fragment.app.Fragment;

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
import ln_zap.zap.fragments.HistoryFragment;
import ln_zap.zap.fragments.SettingsFragment;
import ln_zap.zap.fragments.WalletFragment;
import ln_zap.zap.connection.HttpClient;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.ZapLog;

public class HomeActivity extends BaseAppCompatActivity implements LifecycleObserver {

    private static final String LOG_TAG = "Main Activity";

    private ScheduledExecutorService mExchangeRateScheduler;
    private boolean mIsExchangeRateSchedulerRunning = false;
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

        // ToDo: set the network to testnet on app start. This has to be done on app setup later.
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("mainnet",false);
        editor.apply();

        // Setup fiat exchange rate schedule on first startup
        setupExchangeRateSchedule();

        // Setup Listener
        BottomNavigationView navigation = findViewById(R.id.mainNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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


    private void setupExchangeRateSchedule() {

        if (!mIsExchangeRateSchedulerRunning) {
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
            mIsExchangeRateSchedulerRunning = true;
        }

    }


    // This function gets called when app is moved to foreground.
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        ZapLog.debug(LOG_TAG,"Zap moved to foreground");
        setupExchangeRateSchedule();

        // Restart lnd connection
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            LndConnection.getInstance().restartBackgroundTasks();
        }
    }

    // This function gets called when app is moved to background.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        ZapLog.debug(LOG_TAG,"Zap moved to background");
        if (mIsExchangeRateSchedulerRunning) {
            // Kill the scheduled exchange rate requests to go easy on the battery.
            mExchangeRateScheduler.shutdownNow();
            mIsExchangeRateSchedulerRunning = false;
        }

        // Kill lnd connection
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            LndConnection.getInstance().stopBackgroundTasks();
        }
    }


}
