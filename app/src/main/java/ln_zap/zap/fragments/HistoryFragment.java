package ln_zap.zap.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ln_zap.zap.R;
import ln_zap.zap.util.BlockExplorer;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {


    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Action when clicked on "testButton"
        Button btnTest = view.findViewById(R.id.testButton);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // BlockExplorer.showAddress("2N7JJqJ1SVaa9MqfDiQewzYNGhP87DDidMS", getActivity()); // testnet
               // BlockExplorer.showAddress("3QFK4DzH55gBP9R3gqv3B45y7PNk5vTCZb", getActivity()); // mainnet
                BlockExplorer.showTransaction("a0be518248ab3bbdedee9c5747062d2300f9dae3e1b8f02ba62b206cc80b35d0",getActivity()); // testnet
               // BlockExplorer.showTransaction("dcee0cb33b3496d290701dfdaff5aeb2cfbaef67affa40e4532bfce31088c2ae",getActivity()); // mainnet
            }
        });

        return view;
    }

}
