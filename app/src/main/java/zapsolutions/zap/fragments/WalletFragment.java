package zapsolutions.zap.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.ScanActivity;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.connection.internetConnectionStatus.NetworkUtil;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.customView.WalletSpinner;
import zapsolutions.zap.setup.SetupActivity;
import zapsolutions.zap.util.Balances;
import zapsolutions.zap.util.ExchangeRateUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TorUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class WalletFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        Wallet.BalanceListener, Wallet.InfoListener, Wallet.WalletLoadedListener, ExchangeRateUtil.ExchangeRateListener {

    private static final String LOG_TAG = WalletFragment.class.getName();

    private TextView mTvPrimaryBalance;
    private TextView mTvPrimaryBalanceUnit;
    private TextView mTvSecondaryBalance;
    private TextView mTvSecondaryBalanceUnit;
    private TextView mTvMode;
    private ConstraintLayout mClBalanceLayout;
    private ImageView mIvLogo;
    private ImageView mIvSwitchButton;
    private Animation mBalanceFadeOutAnimation;
    private Animation mLogoFadeInAnimation;
    private ConstraintLayout mWalletConnectedLayout;
    private ConstraintLayout mWalletNotConnectedLayout;
    private ConstraintLayout mLoadingWalletLayout;
    private TextView mTvConnectError;
    private WalletSpinner mWalletSpinner;
    private ImageView mDrawerMenuButton;
    private TextView mWalletNameWidthDummy;
    private ImageView mStatusDot;

    private boolean mPreferenceChangeListenerRegistered = false;
    private boolean mBalanceChangeListenerRegistered = false;
    private boolean mInfoChangeListenerRegistered = false;
    private boolean mExchangeRateListenerRegistered = false;
    private boolean mWalletLoadedListenerRegistered = false;

    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Get View elements
        mClBalanceLayout = view.findViewById(R.id.BalanceLayout);
        mIvLogo = view.findViewById(R.id.logo);
        mIvSwitchButton = view.findViewById(R.id.switchButtonImage);
        mTvPrimaryBalance = view.findViewById(R.id.BalancePrimary);
        mTvPrimaryBalanceUnit = view.findViewById(R.id.BalancePrimaryUnit);
        mTvSecondaryBalance = view.findViewById(R.id.BalanceSecondary);
        mTvSecondaryBalanceUnit = view.findViewById(R.id.BalanceSecondaryUnit);
        mTvMode = view.findViewById(R.id.mode);
        mBalanceFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.balance_fade_out);
        mLogoFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.logo_fade_in);
        mWalletConnectedLayout = view.findViewById(R.id.walletConnected);
        mWalletNotConnectedLayout = view.findViewById(R.id.ConnectionError);
        mLoadingWalletLayout = view.findViewById(R.id.loading);
        mTvConnectError = view.findViewById(R.id.connectError);
        mStatusDot = view.findViewById(R.id.statusDot);
        mWalletSpinner = view.findViewById(R.id.walletSpinner);
        mDrawerMenuButton = view.findViewById(R.id.drawerMenuButton);
        mWalletNameWidthDummy = view.findViewById(R.id.walletNameWidthDummy);

        // Show loading screen
        showLoading();

        mWalletSpinner.setOnWalletSpinnerChangedListener(new WalletSpinner.OnWalletSpinnerChangedListener() {
            @Override
            public void onWalletChanged(String id, String alias) {
                // Close current connection and reset all
                LndConnection.getInstance().closeConnection();
                Wallet.getInstance().reset();
                updateTotalBalanceDisplay();

                // Update status dot
                updateStatusDot(alias);

                // Show loading screen
                showLoading();

                // Clear history list
                ((HomeActivity) getActivity()).getHistoryFragment().updateHistoryDisplayList();

                // Reset drawer menu
                ((HomeActivity) getActivity()).resetDrawerNavigationMenu();

                // Open the newly selected wallet
                ((HomeActivity) getActivity()).openWallet();
            }
        });

        // Update status dot
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            updateStatusDot(WalletConfigsManager.getInstance().getCurrentWalletConfig().getAlias());
        }


        mBalanceFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                mClBalanceLayout.setVisibility(View.VISIBLE);
                mIvSwitchButton.setVisibility(View.VISIBLE);
                mIvLogo.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                mClBalanceLayout.setVisibility(View.INVISIBLE);
                mIvSwitchButton.setVisibility(View.INVISIBLE);
                mIvLogo.setVisibility(View.VISIBLE);
                mIvLogo.startAnimation(mLogoFadeInAnimation);
            }
        });

        // Hide balance if the setting was chosen
        if (PrefsUtil.getPrefs().getBoolean("hideTotalBalance", false)) {
            hideBalance();
        }

        // Action when clicked on menu button
        mDrawerMenuButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((HomeActivity) getActivity()).mDrawer.openDrawer(GravityCompat.START);
            }
        });

        // Action when clicked on "History Button"
        ImageView historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((HomeActivity) getActivity()).mViewPager.setCurrentItem(1);
            }
        });

        // Action when clicked on the logo
        mIvLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBalanceFadeOutAnimation.reset();
                mClBalanceLayout.startAnimation(mBalanceFadeOutAnimation);
                mIvSwitchButton.startAnimation(mBalanceFadeOutAnimation);
            }
        });


        // Swap action when clicked on balance or cancel the fade out in case balance is hidden
        mClBalanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PrefsUtil.getPrefs().getBoolean("hideTotalBalance", false)) {
                    MonetaryUtil.getInstance().switchCurrencies();
                } else {
                    mBalanceFadeOutAnimation.reset();
                    mClBalanceLayout.startAnimation(mBalanceFadeOutAnimation);
                    mIvSwitchButton.startAnimation(mBalanceFadeOutAnimation);
                }
            }
        });

        mClBalanceLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String balances = "On-Chain confirmed: " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Wallet.getInstance().getBalances().onChainConfirmed())
                        + "\nOn-Chain unconfirmed: " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Wallet.getInstance().getBalances().onChainUnconfirmed())
                        + "\nChannel balance: " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Wallet.getInstance().getBalances().channelBalance())
                        + "\nChannel pending: " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Wallet.getInstance().getBalances().channelBalancePending());
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                        .setMessage(balances)
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
                return false;
            }
        });

        // Swap action when clicked swap icon next to balance
        mIvSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonetaryUtil.getInstance().switchCurrencies();

                // also cancel fade out if hideTotalBalance option is active
                if (PrefsUtil.getPrefs().getBoolean("hideTotalBalance", false)) {
                    mBalanceFadeOutAnimation.reset();
                    mClBalanceLayout.startAnimation(mBalanceFadeOutAnimation);
                    mIvSwitchButton.startAnimation(mBalanceFadeOutAnimation);
                }
            }
        });

        // Action when clicked on "scan"
        View btnScan = view.findViewById(R.id.scanButton);
        btnScan.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, HomeActivity.REQUEST_CODE_GENERIC_SCAN);
            }
        });

        // Action when clicked on "receive"
        Button btnReceive = view.findViewById(R.id.receiveButton);
        btnReceive.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ReceiveBSDFragment receiveBottomSheetDialog = new ReceiveBSDFragment();
                receiveBottomSheetDialog.show(getParentFragmentManager(), "receiveBottomSheetDialog");
            }
        });

        // Action when clicked on "setup wallet"
        Button btnSetup = view.findViewById(R.id.setupWallet);
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            btnSetup.setVisibility(View.INVISIBLE);
        }
        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetupActivity.class);
                intent.putExtra(RefConstants.SETUP_MODE, SetupActivity.FULL_SETUP);
                startActivity(intent);
            }
        });


        // Action when clicked on "retry"
        Button btnReconnect = view.findViewById(R.id.reconnectBtn);
        btnReconnect.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (TorUtil.isCurrentConnectionTor() && !TorUtil.isOrbotInstalled(getActivity())) {
                    TorUtil.askToInstallOrbotIfMissing(getActivity());
                } else {
                    mWalletConnectedLayout.setVisibility(View.GONE);
                    mWalletNotConnectedLayout.setVisibility(View.GONE);
                    mLoadingWalletLayout.setVisibility(View.VISIBLE);
                    Wallet.getInstance().checkIfLndIsReachableAndTriggerWalletLoadedInterface();
                }
            }
        });


        updateTotalBalanceDisplay();


        if (App.getAppContext().connectionToLNDEstablished) {
            connectionToLNDEstablished();
        } else {
            if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                if (!LndConnection.getInstance().isConnected()) {
                    LndConnection.getInstance().openConnection();
                }

                Wallet.getInstance().checkIfLndIsReachableAndTriggerWalletLoadedInterface();
            }
        }

        return view;
    }

    private void connectionToLNDEstablished() {

        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {

            // Show info about mode (offline, testnet or mainnet) if it is already known
            onInfoUpdated(Wallet.getInstance().isInfoFetched());

            // Fetch the current balance and info from LND
            Wallet.getInstance().fetchBalanceFromLND();
        }
    }

    private void updateTotalBalanceDisplay() {

        // Adapt unit text size depending on its length
        if (MonetaryUtil.getInstance().getPrimaryDisplayUnit().length() > 2) {
            mTvPrimaryBalanceUnit.setTextSize(20);
        } else {
            mTvPrimaryBalanceUnit.setTextSize(32);
        }

        Balances balances;
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            balances = Wallet.getInstance().getBalances();
        } else {
            balances = Wallet.getInstance().getDemoBalances();
        }

        mTvPrimaryBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmount(balances.total()));
        mTvPrimaryBalanceUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mTvSecondaryBalance.setText(MonetaryUtil.getInstance().getSecondaryDisplayAmount(balances.total()));
        mTvSecondaryBalanceUnit.setText(MonetaryUtil.getInstance().getSecondaryDisplayUnit());

        ZapLog.debug(LOG_TAG, "Total balance display updated");

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update if primary currency has been switched from this or another activity
        if (key.equals("firstCurrencyIsPrimary")) {
            updateTotalBalanceDisplay();
        }
        if (key.equals("hideTotalBalance")) {
            if (PrefsUtil.getPrefs().getBoolean("hideTotalBalance", false)) {
                hideBalance();
            } else {
                showBalance();
            }
        }
    }

    @Override
    public void onExchangeRatesUpdated() {
        updateTotalBalanceDisplay();
    }

    @Override
    public void onBalanceUpdated() {
        updateTotalBalanceDisplay();
    }

    @Override
    public void onInfoUpdated(boolean connected) {
        if (connected) {
            mWalletConnectedLayout.setVisibility(View.VISIBLE);
            mLoadingWalletLayout.setVisibility(View.GONE);
            mWalletNotConnectedLayout.setVisibility(View.GONE);

            if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                if (Wallet.getInstance().isTestnet()) {
                    mTvMode.setText("TESTNET");
                    mTvMode.setVisibility(View.VISIBLE);
                } else {
                    mTvMode.setVisibility(View.GONE);
                }
            } else {
                // Wallet is not setup
                mTvMode.setVisibility(View.GONE);
            }
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superGreen)));
        } else {
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superRed)));
            mWalletConnectedLayout.setVisibility(View.GONE);
            mLoadingWalletLayout.setVisibility(View.GONE);
            mWalletNotConnectedLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // Register listeners
        if (!mPreferenceChangeListenerRegistered) {
            PrefsUtil.getPrefs().registerOnSharedPreferenceChangeListener(this);
            mPreferenceChangeListenerRegistered = true;
        }
        if (!mBalanceChangeListenerRegistered) {
            Wallet.getInstance().registerBalanceListener(this);
            mBalanceChangeListenerRegistered = true;
        }
        if (!mInfoChangeListenerRegistered) {
            Wallet.getInstance().registerInfoListener(this);
            mInfoChangeListenerRegistered = true;
        }
        if (!mWalletLoadedListenerRegistered) {
            Wallet.getInstance().registerWalletLoadedListener(this);
            mWalletLoadedListenerRegistered = true;
        }
        if (!mExchangeRateListenerRegistered) {
            ExchangeRateUtil.getInstance().registerExchangeRateListener(this);
            mExchangeRateListenerRegistered = true;
        }

        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            mWalletSpinner.updateList();
            mWalletSpinner.setVisibility(View.VISIBLE);
        } else {
            mWalletSpinner.setVisibility(View.GONE);
        }

        if (!WalletConfigsManager.getInstance().hasAnyConfigs()) {
            // If the App is not setup yet,
            // this will cause to get the status text updated. Otherwise it would be empty.
            Wallet.getInstance().simulateFetchInfoForDemo(NetworkUtil.isConnectedToInternet(getActivity()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister listeners
        PrefsUtil.getPrefs().unregisterOnSharedPreferenceChangeListener(this);
        Wallet.getInstance().unregisterBalanceListener(this);
        Wallet.getInstance().unregisterInfoListener(this);
        Wallet.getInstance().unregisterWalletLoadedListener(this);
        ExchangeRateUtil.getInstance().unregisterExchangeRateListener(this);
    }

    @Override
    public void onWalletLoadedUpdated(boolean success, int error) {
        if (success) {
            connectionToLNDEstablished();
        } else {
            if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                if (error != Wallet.WalletLoadedListener.ERROR_LOCKED) {
                    onInfoUpdated(false);
                    if (error == Wallet.WalletLoadedListener.ERROR_AUTHENTICATION) {
                        mTvConnectError.setText(R.string.error_connection_invalid_macaroon2);
                    } else if (error == Wallet.WalletLoadedListener.ERROR_TIMEOUT) {
                        mTvConnectError.setText(getResources().getString(R.string.error_connection_server_unreachable, LndConnection.getInstance().getConnectionConfig().getHost()));
                    } else if (error == Wallet.WalletLoadedListener.ERROR_UNAVAILABLE) {
                        mTvConnectError.setText(getResources().getString(R.string.error_connection_lnd_unavailable, String.valueOf(LndConnection.getInstance().getConnectionConfig().getPort())));
                    } else if (error == Wallet.WalletLoadedListener.ERROR_TOR) {
                        mTvConnectError.setText(R.string.error_connection_tor_unreachable);
                    }
                }
            } else {
                onInfoUpdated(true);
            }
        }
    }

    public void showErrorAfterNotUnlocked() {
        mWalletConnectedLayout.setVisibility(View.GONE);
        mWalletNotConnectedLayout.setVisibility(View.VISIBLE);
        mLoadingWalletLayout.setVisibility(View.GONE);

        mTvConnectError.setText(R.string.error_connection_wallet_locked);
    }

    public void showBackgroundForWalletUnlock() {
        mWalletConnectedLayout.setVisibility(View.GONE);
        mWalletNotConnectedLayout.setVisibility(View.GONE);
        mLoadingWalletLayout.setVisibility(View.GONE);
    }

    public void showLoading() {
        mWalletConnectedLayout.setVisibility(View.GONE);
        mWalletNotConnectedLayout.setVisibility(View.GONE);
        mLoadingWalletLayout.setVisibility(View.VISIBLE);
    }

    private void hideBalance() {
        mClBalanceLayout.setVisibility(View.INVISIBLE);
        mIvSwitchButton.setVisibility(View.INVISIBLE);
        mIvLogo.setVisibility(View.VISIBLE);
    }

    private void showBalance() {
        mClBalanceLayout.setVisibility(View.VISIBLE);
        mIvSwitchButton.setVisibility(View.VISIBLE);
        mIvLogo.setVisibility(View.INVISIBLE);
    }

    private void updateStatusDot(String walletAlias) {
        mWalletNameWidthDummy.setText(walletAlias);
        if (NetworkUtil.getConnectivityStatusString(getActivity()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superRed)));
        } else {
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.lightningOrange)));
        }
    }

    private void showError(String message, int duration) {
        Snackbar msg = Snackbar.make(getActivity().findViewById(R.id.mainContent), message, Snackbar.LENGTH_LONG);
        View sbView = msg.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.superRed));
        msg.setDuration(duration);
        msg.show();
    }
}
