package ln_zap.zap.Setup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import ln_zap.zap.R;


public class EulaFragment extends Fragment {


    private CheckBox mAcceptCheckBox;

    public EulaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eula, container, false);

        mAcceptCheckBox = view.findViewById(R.id.checkBox);

        Button btn = view.findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAcceptCheckBox.isChecked()) {
                    ((SetupActivity) getActivity()).eulaAccepted();
                }
            }
        });

        return view;
    }

}
