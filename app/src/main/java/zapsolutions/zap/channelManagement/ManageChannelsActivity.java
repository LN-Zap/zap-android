package zapsolutions.zap.channelManagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.fragments.OpenChannelBSDFragment;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lnurl.channel.LnUrlChannelBSDFragment;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class ManageChannelsActivity extends BaseAppCompatActivity implements ChannelSelectListener, SwipeRefreshLayout.OnRefreshListener, Wallet.ChannelsUpdatedSubscriptionListener {

    private static final String LOG_TAG = ManageChannelsActivity.class.getName();

    private static int REQUEST_CODE_OPEN_CHANNEL = 100;
    private RecyclerView mRecyclerView;
    private ChannelItemAdapter mAdapter;
    private TextView mEmptyListText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<ChannelListItem> mChannelItems;
    private String mCurrentSearchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);

        Wallet.getInstance().registerChannelsUpdatedSubscriptionListener(this);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.seaBlueGradient3));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));

        mRecyclerView = findViewById(R.id.channelsList);
        mEmptyListText = findViewById(R.id.listEmpty);

        mChannelItems = new ArrayList<>();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ManageChannelsActivity.this, ScanNodePubKeyActivity.class);
            startActivityForResult(intent, REQUEST_CODE_OPEN_CHANNEL);
        });
        mAdapter = new ChannelItemAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Display the current state of channels
        updateChannelsDisplayList();

        // Refetch channels from LND. This will automatically update the view when finished.
        // This is necessary, as we might display outdated data otherwise.
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            Wallet.getInstance().fetchChannelsFromLND();
        }
    }

    private void updateChannelsDisplayList() {
        mChannelItems.clear();

        List<ChannelListItem> offlineChannels = new ArrayList<>();

        // Add all open channel items

        if (Wallet.getInstance().mOpenChannelsList != null) {
            for (Channel c : Wallet.getInstance().mOpenChannelsList) {
                OpenChannelItem openChannelItem = new OpenChannelItem(c);
                if (c.getActive()) {
                    mChannelItems.add(openChannelItem);
                } else {
                    offlineChannels.add(openChannelItem);
                }
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

        // Show offline channels at the bottom
        mChannelItems.addAll(offlineChannels);

        // Show "No channels" if the list is empty
        if (mChannelItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }

        // Update the view
        if (mCurrentSearchString.isEmpty()) {
            mAdapter.replaceAll(mChannelItems);
        } else {
            final List<ChannelListItem> filteredContactList = filter(mChannelItems, mCurrentSearchString);
            mAdapter.replaceAll(filteredContactList);
        }
    }

    @Override
    public void onChannelSelect(ByteString channel, int type) {
        if (channel != null) {
            ChannelDetailBSDFragment channelDetailBSDFragment = new ChannelDetailBSDFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ChannelDetailBSDFragment.ARGS_CHANNEL, channel);
            bundle.putInt(ChannelDetailBSDFragment.ARGS_TYPE, type);
            channelDetailBSDFragment.setArguments(bundle);
            channelDetailBSDFragment.show(getSupportFragmentManager(), ChannelDetailBSDFragment.TAG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_CHANNEL && resultCode == ScanNodePubKeyActivity.RESULT_CODE_NODE_URI) {
            if (data != null) {
                LightningNodeUri nodeUri = (LightningNodeUri) data.getSerializableExtra(ScanNodePubKeyActivity.EXTRA_NODE_URI);

                OpenChannelBSDFragment openChannelBSDFragment = new OpenChannelBSDFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(OpenChannelBSDFragment.ARGS_NODE_URI, nodeUri);
                openChannelBSDFragment.setArguments(bundle);
                openChannelBSDFragment.show(getSupportFragmentManager(), OpenChannelBSDFragment.TAG);
            }
        }

        if (requestCode == REQUEST_CODE_OPEN_CHANNEL && resultCode == ScanNodePubKeyActivity.RESULT_CODE_LNURL_CHANNEL) {
            if (data != null) {
                LnUrlChannelResponse channelResponse = (LnUrlChannelResponse) data.getSerializableExtra(ScanNodePubKeyActivity.EXTRA_CHANNEL_RESPONSE);
                LnUrlChannelBSDFragment lnUrlChannelBSDFragment = LnUrlChannelBSDFragment.createLnURLChannelDialog(channelResponse);
                lnUrlChannelBSDFragment.show(getSupportFragmentManager(), LnUrlChannelBSDFragment.TAG);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Wallet.getInstance().unregisterChannelsUpdatedSubscriptionListener(this);

        super.onDestroy();
    }

    @Override
    public void onChannelsUpdated() {
        runOnUiThread(this::updateChannelsDisplayList);
        mSwipeRefreshLayout.setRefreshing(false);
        ZapLog.d(LOG_TAG, "Channels updated!");
    }

    @Override
    public void onRefresh() {
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            Wallet.getInstance().fetchChannelsFromLND();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchButton);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCurrentSearchString = newText;
                final List<ChannelListItem> filteredContactList = filter(mChannelItems, newText);
                mAdapter.replaceAll(filteredContactList);
                mRecyclerView.scrollToPosition(0);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private List<ChannelListItem> filter(List<ChannelListItem> items, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<ChannelListItem> filteredItemList = new ArrayList<>();
        for (ChannelListItem item : items) {
            String text;
            String pubkey;

            switch (item.getType()) {
                case ChannelListItem.TYPE_OPEN_CHANNEL:
                    pubkey = ((OpenChannelItem) item).getChannel().getRemotePubkey();
                    text = pubkey + Wallet.getInstance().getNodeAliasFromPubKey(pubkey, ManageChannelsActivity.this).toLowerCase();
                    break;
                case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                    pubkey = ((PendingOpenChannelItem) item).getChannel().getChannel().getRemoteNodePub();
                    text = pubkey + Wallet.getInstance().getNodeAliasFromPubKey(pubkey, ManageChannelsActivity.this).toLowerCase();
                    break;
                case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                    pubkey = ((PendingClosingChannelItem) item).getChannel().getChannel().getRemoteNodePub();
                    text = pubkey + Wallet.getInstance().getNodeAliasFromPubKey(pubkey, ManageChannelsActivity.this).toLowerCase();
                    break;
                case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                    pubkey = ((PendingForceClosingChannelItem) item).getChannel().getChannel().getRemoteNodePub();
                    text = pubkey + Wallet.getInstance().getNodeAliasFromPubKey(pubkey, ManageChannelsActivity.this).toLowerCase();
                    break;
                case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                    pubkey = ((WaitingCloseChannelItem) item).getChannel().getChannel().getRemoteNodePub();
                    text = pubkey + Wallet.getInstance().getNodeAliasFromPubKey(pubkey, ManageChannelsActivity.this).toLowerCase();
                    break;
                case ChannelListItem.TYPE_CLOSED_CHANNEL:
                    text = "";
                default:
                    text = "";
            }

            if (text.contains(lowerCaseQuery)) {
                filteredItemList.add(item);
            }
        }
        return filteredItemList;
    }
}
