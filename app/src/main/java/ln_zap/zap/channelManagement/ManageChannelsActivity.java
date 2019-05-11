package ln_zap.zap.channelManagement;

import android.os.Bundle;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ln_zap.zap.R;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;

public class ManageChannelsActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = "Manage Channels Activity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mEmptyListText;

    private List<ChannelListItem> mChannelItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);


        mRecyclerView = findViewById(R.id.channelsList);
        mEmptyListText = findViewById(R.id.listEmpty);

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

    private void updateChannelsDisplayList() {
        mChannelItems.clear();

        // Add all open channel items

        if (Wallet.getInstance().mOpenChannelsList != null) {
            for (Channel c : Wallet.getInstance().mOpenChannelsList) {
                OpenChannelItem openChannelItem = new OpenChannelItem(c);
                mChannelItems.add(openChannelItem);
            }
        }


        // Add all pending channel items

        // Add open pending
        if (Wallet.getInstance().mPendingOpenChannelsList != null) {
            for (PendingChannelsResponse.PendingOpenChannel c : Wallet.getInstance().mPendingOpenChannelsList) {
                PendingOpenChannelItem pendingOpenChannelItem = new PendingOpenChannelItem(c);
                mChannelItems.add(pendingOpenChannelItem);
            }
        }

        // Add closing pending
        if (Wallet.getInstance().mPendingClosedChannelsList != null) {
            for (PendingChannelsResponse.ClosedChannel c : Wallet.getInstance().mPendingClosedChannelsList) {
                PendingClosingChannelItem pendingClosingChannelItem = new PendingClosingChannelItem(c);
                mChannelItems.add(pendingClosingChannelItem);
            }
        }

        // Add force closing pending
        if (Wallet.getInstance().mPendingForceClosedChannelsList != null) {
            for (PendingChannelsResponse.ForceClosedChannel c : Wallet.getInstance().mPendingForceClosedChannelsList) {
                PendingForceClosingChannelItem pendingForceClosingChannelItem = new PendingForceClosingChannelItem(c);
                mChannelItems.add(pendingForceClosingChannelItem);
            }
        }

        // Add waiting for close
        if (Wallet.getInstance().mPendingWaitingCloseChannelsList != null) {
            for (PendingChannelsResponse.WaitingCloseChannel c : Wallet.getInstance().mPendingWaitingCloseChannelsList) {
                WaitingCloseChannelItem waitingCloseChannelItem = new WaitingCloseChannelItem(c);
                mChannelItems.add(waitingCloseChannelItem);
            }
        }


        // Show "No channels" if the list is empty

        if (mChannelItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }


        // Show the list

        mAdapter = new ChannelItemAdapter(mChannelItems);
        mRecyclerView.setAdapter(mAdapter);
    }

}
