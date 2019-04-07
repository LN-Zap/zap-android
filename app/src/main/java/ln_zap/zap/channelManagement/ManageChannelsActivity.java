package ln_zap.zap.channelManagement;

import android.os.Bundle;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.util.Wallet;

public class ManageChannelsActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = "Manage Channels Activity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<ChannelListItem> mChannelItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);


        mRecyclerView = findViewById(R.id.channelsList);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mChannelItems = new ArrayList<>();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.coming_soon, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        updateChannelsDisplayList();
    }

    private void updateChannelsDisplayList(){
        mChannelItems.clear();

        // Add all open channel items

        if (Wallet.getInstance().mOpenChannelsList != null) {
            for (Channel c : Wallet.getInstance().mOpenChannelsList) {
                OpenChannelItem openChannelItem = new OpenChannelItem(c);
                mChannelItems.add(openChannelItem);
            }
        }


        // Show the list

        mAdapter = new ChannelItemAdapter(mChannelItems);
        mRecyclerView.setAdapter(mAdapter);
    }

}
