package zapsolutions.zap.setup;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import zapsolutions.zap.R;


public class ConnectFragment extends Fragment {


    public ConnectFragment() {
        // Required empty public constructor
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
                startActivity(intent);
            }
        });

        return view;
    }

}
