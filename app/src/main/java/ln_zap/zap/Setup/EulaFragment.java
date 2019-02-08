package ln_zap.zap.Setup;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
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

                    // Set the eulaAccepted preference to true to ensure the app only asks once for this.
                    SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("eulaAccepted", true);
                    editor.apply();

                    // Go to next setup step.
                    ((SetupActivity) getActivity()).eulaAccepted();
                }
            }
        });

        return view;
    }

}
