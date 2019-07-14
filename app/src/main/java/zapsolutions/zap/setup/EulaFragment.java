package zapsolutions.zap.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import zapsolutions.zap.R;
import zapsolutions.zap.util.PrefsUtil;


public class EulaFragment extends Fragment {


    private CheckBox mAcceptCheckBox;
    private Button mNextButton;

    public EulaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eula, container, false);

        mAcceptCheckBox = view.findViewById(R.id.checkBox);
        mNextButton = view.findViewById(R.id.button);

        mAcceptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNextButton.setEnabled(isChecked);
                if (isChecked) {
                    mNextButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightningOrange));
                } else {
                    mNextButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                }
            }
        });

        Button btn = view.findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the eulaAccepted preference to true to ensure the app only asks once for this.
                PrefsUtil.edit().putBoolean("eulaAccepted", true).apply();

                // Go to next setup step.
                ((SetupActivity) getActivity()).eulaAccepted();
            }
        });

        return view;
    }

}
