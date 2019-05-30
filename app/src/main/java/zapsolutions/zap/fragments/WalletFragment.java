package zapsolutions.zap.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.NetworkUtil;
import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.setup.SetupActivity;
import zapsolutions.zap.SendActivity;
import zapsolutions.zap.util.Balances;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class WalletFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        Wallet.BalanceListener, Wallet.InfoListener, Wallet.WalletLoadedListener, MonetaryUtil.ExchangeRateListener, UserGuardianInterface {

    private static final String LOG_TAG = "Wallet Fragment";

    private UserGuardian mUG;
    private SharedPreferences mPrefs;
    private TextView mTvPrimaryBalance;
    private TextView mTvPrimaryBalanceUnit;
    private TextView mTvSecondaryBalance;
    private TextView mTvSecondaryBalanceUnit;
    private TextView mTvBtcRate;
    private TextView mTvMode;
    private ConstraintLayout mClBalanceLayout;
    private ImageView mIvLogo;
    private ImageView mIvSwitchButton;
    private Animation mBalanceFadeOutAnimation;
    private Animation mLogoFadeInAnimation;
    private FragmentManager mFragmentManager;
    private ConstraintLayout mWalletConnectedLayout;
    private ConstraintLayout mWalletNotConnectedLayout;
    private ConstraintLayout mLoadingWalletLayout;

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

        mUG = new UserGuardian(getActivity(), this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mFragmentManager = getFragmentManager();


        // Get View elements
        mClBalanceLayout = view.findViewById(R.id.BalanceLayout);
        mIvLogo = view.findViewById(R.id.logo);
        mIvSwitchButton = view.findViewById(R.id.switchButtonImage);
        mTvPrimaryBalance = view.findViewById(R.id.BalancePrimary);
        mTvPrimaryBalanceUnit = view.findViewById(R.id.BalancePrimaryUnit);
        mTvSecondaryBalance = view.findViewById(R.id.BalanceSecondary);
        mTvSecondaryBalanceUnit = view.findViewById(R.id.BalanceSecondaryUnit);
        mTvBtcRate = view.findViewById(R.id.btcRate);
        mTvMode = view.findViewById(R.id.mode);
        mBalanceFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.balance_fade_out);
        mLogoFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.logo_fade_in);
        mWalletConnectedLayout = view.findViewById(R.id.walletConnected);
        mWalletNotConnectedLayout = view.findViewById(R.id.ConnectionError);
        mLoadingWalletLayout = view.findViewById(R.id.loading);

        mWalletConnectedLayout.setVisibility(View.GONE);
        mLoadingWalletLayout.setVisibility(View.VISIBLE);

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
        if (mPrefs.getBoolean("hideTotalBalance", false)) {
            mClBalanceLayout.setVisibility(View.INVISIBLE);
            mIvSwitchButton.setVisibility(View.INVISIBLE);
            mIvLogo.setVisibility(View.VISIBLE);
        }

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
                if (!mPrefs.getBoolean("hideTotalBalance", false)) {
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
                if (mPrefs.getBoolean("preventScreenRecording", true)) {
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
                if (mPrefs.getBoolean("hideTotalBalance", false)) {
                    mBalanceFadeOutAnimation.reset();
                    mClBalanceLayout.startAnimation(mBalanceFadeOutAnimation);
                    mIvSwitchButton.startAnimation(mBalanceFadeOutAnimation);
                }
            }
        });

        // Action when clicked on "send"
        Button btnSend = view.findViewById(R.id.sendButton);
        btnSend.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(getActivity(), SendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, 1);
            }
        });


        // Action when clicked on "receive"
        Button btnReceive = view.findViewById(R.id.receiveButton);
        btnReceive.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ReceiveBSDFragment receiveBottomSheetDialog = new ReceiveBSDFragment();
                receiveBottomSheetDialog.show(mFragmentManager, "receiveBottomSheetDialog");
            }
        });

        // Action when clicked on "setup wallet"
        Button btnSetup = view.findViewById(R.id.setupWallet);
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            btnSetup.setVisibility(View.INVISIBLE);
        }
        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetupActivity.class);
                intent.putExtra("setupMode", SetupActivity.FULL_SETUP);
                startActivity(intent);
            }
        });


        updateTotalBalanceDisplay();


        if (App.getAppContext().connectionToLNDEstablished) {
            connectionToLNDEstablished();
        } else {
            if (mPrefs.getBoolean("isWalletSetup", false)) {
                Wallet.getInstance().isLNDReachable();
            }
        }

        // if the wallet is not setup we still want to show the wallet and an error if a payment url was used.
        if (!mPrefs.getBoolean("isWalletSetup", false)) {
            mWalletConnectedLayout.setVisibility(View.VISIBLE);
            mLoadingWalletLayout.setVisibility(View.GONE);
            if (App.getAppContext().getUriSchemeData() != null) {
                Intent intent = new Intent(getActivity(), SendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, 1);
            }
        }

        return view;
    }

    private void connectionToLNDEstablished() {

        if (mPrefs.getBoolean("isWalletSetup", false)) {

            // Show info about mode (offline, testnet or mainnet) if it is already known
            onInfoUpdated(Wallet.getInstance().isInfoFetched());

            // Fetch the current balance and info from LND
            Wallet.getInstance().fetchBalanceFromLND();
        }

        // check if we have an URI Scheme present. If there is one, start the send activity,
        // which will then immediately start to validate it.
        if (App.getAppContext().getUriSchemeData() != null) {
            Intent intent = new Intent(getActivity(), SendActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, 1);
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
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            balances = Wallet.getInstance().getBalances();
        } else {
            balances = Wallet.getInstance().getDemoBalances();
        }

        mTvPrimaryBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmount(balances.total()));
        mTvPrimaryBalanceUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mTvSecondaryBalance.setText(MonetaryUtil.getInstance().getSecondaryDisplayAmount(balances.total()));
        mTvSecondaryBalanceUnit.setText(MonetaryUtil.getInstance().getSecondaryDisplayUnit());

        if (MonetaryUtil.getInstance().getSecondCurrency().isBitcoin()) {
            // Hide btc rate info if both units are btc
            mTvBtcRate.setVisibility(View.GONE);
        } else {
            String rate;
            if (MonetaryUtil.getInstance().getPrimaryCurrency().isBitcoin()) {
                rate = MonetaryUtil.getInstance().getSecondaryDisplayAmountAndUnit(100000000);
            } else {
                rate = MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(100000000);
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                rate = "1 \u20BF ≈ " + rate;
            } else{
                rate = "1 BTC ≈ " + rate;
            }

            mTvBtcRate.setText(rate);
            mTvBtcRate.setVisibility(View.VISIBLE);
        }
        ZapLog.debug(LOG_TAG, "Total balance display updated");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed. Here it is 1
        if (requestCode == 1) {
            // This gets executed if the a vaild payment request was scanned or pasted
            if (data != null) {
                if (data.getExtras().getString("error") == null) {
                    boolean onChain = data.getExtras().getBoolean("onChain");
                    if (onChain) {
                        long amount = data.getExtras().getLong("onChainAmount");
                        String address = data.getExtras().getString("onChainAddress");
                        String message = data.getExtras().getString("onChainMessage");
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("onChain", onChain);
                        bundle.putLong("onChainAmount", amount);
                        bundle.putString("onChainAddress", address);
                        bundle.putString("onChainMessage", message);

                        SendBSDFragment sendBottomSheetDialog = new SendBSDFragment();
                        sendBottomSheetDialog.setArguments(bundle);
                        sendBottomSheetDialog.show(mFragmentManager, "sendBottomSheetDialog");
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("onChain", onChain);

                        SendBSDFragment sendBottomSheetDialog = new SendBSDFragment();
                        sendBottomSheetDialog.setArguments(bundle);
                        sendBottomSheetDialog.show(mFragmentManager, "sendBottomSheetDialog");
                    }
                } else {
                    ZapLog.debug(LOG_TAG, "Error arrived!");
                    showError(data.getExtras().getString("error"), data.getExtras().getInt("error_duration"));
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update if primary currency has been switched from this or another activity
        if (key.equals("firstCurrencyIsPrimary")) {
            updateTotalBalanceDisplay();
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

            if ((mPrefs.getBoolean("isWalletSetup", false))) {
                if (Wallet.getInstance().isTestnet()) {
                    mTvMode.setText("TESTNET");
                    mTvMode.setTextColor(ContextCompat.getColor(getActivity(), R.color.superGreen));
                    mTvMode.setVisibility(View.VISIBLE);
                } else {
                    mTvMode.setText("");
                    mTvMode.setVisibility(View.GONE);
                }
            } else {
                // Wallet is not setup
                mTvMode.setText("");
                mTvMode.setVisibility(View.GONE);
            }
        } else {
            if (NetworkUtil.getConnectivityStatusString(getActivity()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                mTvMode.setText(getActivity().getResources().getString(R.string.offline).toUpperCase());
                mTvMode.setTextColor(ContextCompat.getColor(getActivity(), R.color.superRed));
                mTvMode.setVisibility(View.VISIBLE);
            } else {
                mWalletConnectedLayout.setVisibility(View.GONE);
                mLoadingWalletLayout.setVisibility(View.GONE);
                mWalletNotConnectedLayout.setVisibility(View.VISIBLE);
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // Register listeners
        if (!mPreferenceChangeListenerRegistered) {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
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
            MonetaryUtil.getInstance().registerExchangeRateListener(this);
            mExchangeRateListenerRegistered = true;
        }


        if (!mPrefs.getBoolean("isWalletSetup", false)) {
            // If the App is not setup yet,
            // this will cause to get the status text updated. Otherwise it would be empty.
            Wallet.getInstance().simulateFetchInfoForDemo(NetworkUtil.isConnectedToInternet(getActivity()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister listeners
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        Wallet.getInstance().unregisterBalanceListener(this);
        Wallet.getInstance().unregisterInfoListener(this);
        Wallet.getInstance().unregisterWalletLoadedListener(this);
        MonetaryUtil.getInstance().unregisterExchangeRateListener(this);
    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {

    }

    @Override
    public void onWalletLoadedUpdated(boolean success, String error) {
        if(success) {
            connectionToLNDEstablished();
        } else {
            // Show info about mode (offline, testnet or mainnet) if it is already known
            if (mPrefs.getBoolean("isWalletSetup", false)) {
                onInfoUpdated(false);
            } else {
                onInfoUpdated(true);
            }
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
