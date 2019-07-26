package zapsolutions.zap.setup;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;


public class ConnectFragment extends Fragment {

    public static final int MODE_ADD = 0;
    public static final int MODE_MODIFY = 1;
    private static final String ARG_MODE = "mode";

    private int mMode;

    public ConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mode set the mode to either add or modify
     * @return A new instance of fragment ConnectFragment.
     */
    public static ConnectFragment newInstance(int mode) {
        ConnectFragment fragment = new ConnectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMode = getArguments().getInt(ARG_MODE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        // Action when clicked on "connect to remote node"
        Button btnConnectToRemote = view.findViewById(R.id.connect_btnRemoteNode);
        btnConnectToRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ConnectRemoteNodeActivity.class);
                if (mMode == MODE_MODIFY){
                    intent.putExtra("walletAlias", WalletConfigsManager.getInstance().getConnectionToModify().getAlias());
                }
                startActivity(intent);
            }
        });

        return view;
    }

}
