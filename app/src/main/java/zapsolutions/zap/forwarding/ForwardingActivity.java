package zapsolutions.zap.forwarding;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.lightningnetwork.lnd.lnrpc.ForwardingEvent;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.util.Wallet;

public class ForwardingActivity extends BaseAppCompatActivity implements ForwardingEventSelectListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = ForwardingActivity.class.getName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<ForwardingEventListItem> mForwardingEventItems;
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

        mForwardingEventItems = new ArrayList<>();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(ForwardingActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create and set adapter
        mAdapter = new ForwardingEventItemAdapter(mForwardingEventItems, this);
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
        mForwardingEventItems.clear();
        if (Wallet.getInstance().mForwardingEventsList != null) {

            for (ForwardingEvent forwardingEvent : Wallet.getInstance().mForwardingEventsList) {
                ForwardingEventListItem currItem = new ForwardingEventListItem(forwardingEvent);
                mForwardingEventItems.add(currItem);
            }

            // Show "No forwarding events" if the list is empty
            if (mForwardingEventItems.size() == 0) {
                mEmptyListText.setVisibility(View.VISIBLE);
            } else {
                mEmptyListText.setVisibility(View.GONE);
            }

            // Update the list view
            mAdapter.notifyDataSetChanged();
        }
        // Remove refreshing symbol
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs() && LndConnection.getInstance().isConnected()) {
            // ToDo: refetch data from LND
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
