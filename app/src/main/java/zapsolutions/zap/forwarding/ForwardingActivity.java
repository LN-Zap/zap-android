package zapsolutions.zap.forwarding;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.lightningnetwork.lnd.lnrpc.ForwardingEvent;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.forwarding.listItems.DateItem;
import zapsolutions.zap.forwarding.listItems.ForwardingEventListItem;
import zapsolutions.zap.forwarding.listItems.ForwardingListItem;
import zapsolutions.zap.util.Wallet;

public class ForwardingActivity extends BaseAppCompatActivity implements ForwardingEventSelectListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = ForwardingActivity.class.getName();

    private RecyclerView mRecyclerView;
    private ForwardingEventItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<ForwardingListItem> mForwardingItems;
    private TextView mEmptyListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.seaBlueGradient3));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));

        mRecyclerView = findViewById(R.id.forwardingEventList);
        mEmptyListText = findViewById(R.id.listEmpty);

        mForwardingItems = new ArrayList<>();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(ForwardingActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create and set adapter
        mAdapter = new ForwardingEventItemAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // display current state of the list
        updateForwardingEventDisplayList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ToDo: refetch data from LND
    }

    private void updateForwardingEventDisplayList() {

        // Save state, we want to keep the scroll offset after the update.
        Parcelable recyclerViewState;
        recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();

        mForwardingItems.clear();

        List<ForwardingListItem> forwardingEvents = new LinkedList<>();
        Set<ForwardingListItem> dateLines = new HashSet<>();

        if (Wallet.getInstance().mForwardingEventsList != null) {
            // Add all relevant items the forwardingEvents list
            for (ForwardingEvent forwardingEvent : Wallet.getInstance().mForwardingEventsList) {
                ForwardingEventListItem currItem = new ForwardingEventListItem(forwardingEvent);
                forwardingEvents.add(currItem);
            }
        }
        mForwardingItems.addAll(forwardingEvents);

        // Add the Date Lines
        for (ForwardingListItem item : forwardingEvents) {
            DateItem dateItem = new DateItem(item.getTimestamp());
            dateLines.add(dateItem);
        }
        mForwardingItems.addAll(dateLines);

        // Show "No forwarding events" if the list is empty
        if (mForwardingItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }

        // Update the list view
        mAdapter.replaceAll(mForwardingItems);

        // Set number in activity title
        if (forwardingEvents.size() > 0) {
            String title = getResources().getString(R.string.activity_forwarding) + " (" + forwardingEvents.size() + ")";
            setTitle(title);
        } else {
            setTitle(getResources().getString(R.string.activity_forwarding));
        }

        // Restore state (e.g. scroll offset)
        mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

        // Remove refreshing symbol
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs() && LndConnection.getInstance().isConnected()) {
            // ToDo: refetch data from LND
            updateForwardingEventDisplayList();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onForwardingEventSelect(ByteString forwardingEvent) {
        /*
        Bundle bundle = new Bundle();
        if (routingEvent != null) {
            UTXODetailBSDFragment utxoDetailBSDFragment = new UTXODetailBSDFragment();
            bundle.putSerializable(UTXODetailBSDFragment.ARGS_UTXO, routingEvent);
            utxoDetailBSDFragment.setArguments(bundle);
            utxoDetailBSDFragment.show(getSupportFragmentManager(), UTXODetailBSDFragment.TAG);
        }
         */
    }
}
