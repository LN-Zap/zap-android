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

import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceResponse;

import androidx.preference.PreferenceManager;

import io.grpc.StatusRuntimeException;


import ln_zap.zap.R;
import ln_zap.zap.ReceiveActivity;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.qrCodeScanner.QRCodeScannerActivity;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.ZapLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class Wallet extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "Wallet Fragment";

    private SharedPreferences mPrefs;
    private long mTotalBalance = 0;
    private TextView mTvPrimaryBalance;
    private TextView mTvPrimaryBalanceUnit;
    private TextView mTvSecondaryBalance;
    private TextView mTvSecondaryBalanceUnit;


    public Wallet() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Get View elements
        mTvPrimaryBalance = view.findViewById(R.id.BalancePrimary);
        mTvPrimaryBalanceUnit = view.findViewById(R.id.BalancePrimaryUnit);
        mTvSecondaryBalance = view.findViewById(R.id.BalanceSecondary);
        mTvSecondaryBalanceUnit = view.findViewById(R.id.BalanceSecondaryUnit);

        updateTotalBalanceDisplay();

        // Swap action when clicked on balance
        ConstraintLayout clBalance = view.findViewById(R.id.BalanceLayout);
        clBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonetaryUtil.getInstance().switchCurrencies();
                updateTotalBalanceDisplay();
            }
        });

        // Swap action when clicked swap icon next to balance
        ImageView ivSwapImage = view.findViewById(R.id.switchButtonImage);
        ivSwapImage.setOnClickListener(new View.OnClickListener() {
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



        // gRPC!!!
        // blocking stub
        LightningGrpc.LightningBlockingStub BalanceClient = LightningGrpc
                .newBlockingStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        WalletBalanceRequest balanceRequest = WalletBalanceRequest.newBuilder().build();

        WalletBalanceResponse balanceResponse;

        try {
            balanceResponse = BalanceClient.walletBalance(balanceRequest);
            ZapLog.debug(LOG_TAG,balanceResponse.toString());
            mTotalBalance = balanceResponse.getTotalBalance();
            updateTotalBalanceDisplay();
        }
        catch(StatusRuntimeException e){
            ZapLog.debug(LOG_TAG,"An Error occured on the balance Request");
            e.printStackTrace();
        }

        /*
        // non blocking stub
        LightningGrpc.LightningFutureStub aBalanceClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());


        WalletBalanceRequest aBalanceRequest = WalletBalanceRequest.newBuilder().build();
        */



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

        mTvPrimaryBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmount(mTotalBalance));
        mTvPrimaryBalanceUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mTvSecondaryBalance.setText(MonetaryUtil.getInstance().getSecondaryDisplayAmount(mTotalBalance));
        mTvSecondaryBalanceUnit.setText(MonetaryUtil.getInstance().getSecondaryDisplayUnit());

    }



    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the listener whenever a key changes
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key)
    {
        //update if currency has been switched or new exchange data arrived
        if (key.equals("firstCurrencyIsPrimary") || key.equals("fiat_USD")){
            updateTotalBalanceDisplay();
            ZapLog.debug(LOG_TAG,"Total balance display updated");
        }
    }

}
