package ln_zap.zap.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ln_zap.zap.QRCodeScanner.QRCodeScannerActivity;
import ln_zap.zap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Wallet extends Fragment {


    public Wallet() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);


        Button btnScan = view.findViewById(R.id.scanButton);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), QRCodeScannerActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

            }
        });

        return view;
    }



}
