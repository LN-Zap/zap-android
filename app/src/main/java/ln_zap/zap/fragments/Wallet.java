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
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import ln_zap.zap.R;
import ln_zap.zap.ReceiveActivity;
import ln_zap.zap.qrCodeScanner.QRCodeScannerActivity;
import ln_zap.zap.util.MonetaryUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class Wallet extends Fragment {

    private SharedPreferences prefs;
    private TextView tvPrimaryBalance;
    private TextView tvPrimaryBalanceUnit;
    private TextView tvSecondaryBalance;
    private TextView tvSecondaryBalanceUnit;


    public Wallet() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Get View elements
        tvPrimaryBalance = view.findViewById(R.id.BalancePrimary);
        tvPrimaryBalanceUnit = view.findViewById(R.id.BalancePrimaryUnit);
        tvSecondaryBalance = view.findViewById(R.id.BalanceSecondary);
        tvSecondaryBalanceUnit = view.findViewById(R.id.BalanceSecondaryUnit);

        setBalance();

        // Swap action when clicked on balance
        ConstraintLayout clBalance = view.findViewById(R.id.BalanceLayout);
        clBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prefs.getBoolean("isBitcoinPrimary", true)){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isBitcoinPrimary", false);
                    editor.apply();
                    setBalance();
                }
                else{
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isBitcoinPrimary", true);
                    editor.apply();
                    setBalance();
                }
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

        // temporarily the update of the exchange rates happens here. Later it will have to
        // be scheduled
        MonetaryUtil.getInstance(getActivity()).getExchangeRates();

        return view;
    }


    private void setBalance(){
        // placeholder value
        long myBalance = 120871010L;

            tvPrimaryBalance.setText(MonetaryUtil.getInstance(getActivity()).getPrimaryDisplayAmount(myBalance));
            tvPrimaryBalanceUnit.setText(MonetaryUtil.getInstance(getActivity()).getPrimaryDisplayUnit());
            tvSecondaryBalance.setText(MonetaryUtil.getInstance(getActivity()).getSecondaryDisplayAmount(myBalance));
            tvSecondaryBalanceUnit.setText(MonetaryUtil.getInstance(getActivity()).getSecondaryDisplayUnit());

    }

}
