package ln_zap.zap.fragments;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import ln_zap.zap.R;
import ln_zap.zap.ReceiveActivity;
import ln_zap.zap.setup.SetupActivity;
import ln_zap.zap.qrCodeScanner.QRCodeScannerActivity;
import ln_zap.zap.util.Balances;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class WalletFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        Wallet.BalanceListener,Wallet.InfoListener {

    private static final String LOG_TAG = "Wallet Fragment";

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

    private boolean mPreferenceChangeListenerRegistered = false;
    private boolean mBalanceChangeListenerRegistered = false;
    private boolean mInfoChangeListenerRegistered = false;


    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

        // Hide balance if the setting was chosen
        if(mPrefs.getBoolean("hideTotalBalance", false)){
            mClBalanceLayout.setVisibility(View.GONE);
            mIvSwitchButton.setVisibility(View.GONE);
            mIvLogo.setVisibility(View.VISIBLE);
        }

        // Action when clicked on the logo
        mIvLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClBalanceLayout.setVisibility(View.VISIBLE);
                mIvSwitchButton.setVisibility(View.VISIBLE);
                mIvLogo.setVisibility(View.GONE);
            }
        });

        updateTotalBalanceDisplay();

        // Swap action when clicked on balance
        mClBalanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonetaryUtil.getInstance().switchCurrencies();
                updateTotalBalanceDisplay();
            }
        });

        // Swap action when clicked swap icon next to balance
        mIvSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonetaryUtil.getInstance().switchCurrencies();
                updateTotalBalanceDisplay();
            }
        });

        // Action when clicked on "send"
        Button btnSend = view.findViewById(R.id.sendButton);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QRCodeScannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });


        // Action when clicked on "receive"
        Button btnReceive = view.findViewById(R.id.receiveButton);
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceiveActivity.class);
                startActivity(intent);
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

        // Show info about testnet if it is already known
        if (Wallet.getInstance().isInfoFetched()){
            onInfoUpdated();
        }

        // fetch the current balance and info from LND
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            Wallet.getInstance().fetchBalanceFromLND();
            Wallet.getInstance().fetchInfoFromLND();
        } else {
            mTvMode.setText("DEMO MODE");
        }

        return view;
    }

    private void updateTotalBalanceDisplay(){

        // Adapt unit text size depending on its length
        if (MonetaryUtil.getInstance().getPrimaryDisplayUnit().length() > 2){
            mTvPrimaryBalanceUnit.setTextSize(20);
        }
        else{
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

        if (MonetaryUtil.getInstance().getSecondCurrency().isBitcoin()){
            // Hide btc rate info if both units are btc
            mTvBtcRate.setVisibility(View.GONE);
        } else {
            String rate;
            if (MonetaryUtil.getInstance().getPrimaryCurrency().isBitcoin()) {
                rate = MonetaryUtil.getInstance().getSecondaryDisplayAmountAndUnit(100000000);
            } else {
                rate = MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(100000000);
            }

            rate = "1 BTC ≈ " + rate;
            mTvBtcRate.setText(rate);
            mTvBtcRate.setVisibility(View.VISIBLE);
        }
        ZapLog.debug(LOG_TAG,"Total balance display updated");

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        //update if currency has been switched or new exchange data arrived
        if (key.equals("firstCurrencyIsPrimary") || key.equals("fiat_USD")){
            updateTotalBalanceDisplay();
        }
    }

    @Override
    public void onBalanceUpdated() {
        updateTotalBalanceDisplay();
    }

    @Override
    public void onInfoUpdated() {
        if (Wallet.getInstance().isTestnet()) {
            mTvMode.setText("TESTNET");
            mTvMode.setVisibility(View.VISIBLE);
        }

        else {
            mTvMode.setText("");
            mTvMode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register listeners
        if(!mPreferenceChangeListenerRegistered){
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            mPreferenceChangeListenerRegistered = true;
        }
        if(!mBalanceChangeListenerRegistered) {
            Wallet.getInstance().registerBalanceListener(this);
            mBalanceChangeListenerRegistered = true;
        }
        if(!mInfoChangeListenerRegistered) {
            Wallet.getInstance().registerInfoListener(this);
            mInfoChangeListenerRegistered = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister listeners
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        Wallet.getInstance().unregisterBalanceListener(this);
        Wallet.getInstance().unregisterInfoListener(this);
    }

}
