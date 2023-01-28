package zapsolutions.zap.forwarding;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.lightningnetwork.lnd.lnrpc.ForwardingEvent;
import com.github.lightningnetwork.lnd.lnrpc.ForwardingHistoryRequest;
import com.google.android.material.tabs.TabLayout;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.forwarding.listItems.DateItem;
import zapsolutions.zap.forwarding.listItems.ForwardingEventListItem;
import zapsolutions.zap.forwarding.listItems.ForwardingListItem;
import zapsolutions.zap.tor.TorManager;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ZapLog;

public class ForwardingActivity extends BaseAppCompatActivity implements ForwardingEventSelectListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = ForwardingActivity.class.getName();

    private RecyclerView mRecyclerView;
    private ForwardingEventItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TabLayout mTabLayoutPeriod;
    private long mPeriod = 24 * 60 * 60; // in seconds
    private TextView mTVAmount;
    private TextView mTVUnit;
    private View mVEarnedAmountProgress;

    private List<ForwardingListItem> mForwardingItems;
    private List<ForwardingEvent> mTempForwardingEventsList;
    private List<ForwardingEvent> mForwardingEventsList;
    private TextView mEmptyListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);

        mTabLayoutPeriod = findViewById(R.id.periodTabLayout);
        mTVAmount = findViewById(R.id.amount);
        mTVUnit = findViewById(R.id.unit);
        mVEarnedAmountProgress = findViewById(R.id.earnedFeeProgress);

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

        mTabLayoutPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        // 1 day
                        mPeriod = 24 * 60 * 60;
                        break;
                    case 1:
                        // 1 week
                        mPeriod = 7 * 24 * 60 * 60;
                        break;
                    case 2:
                        // 1 month
                        mPeriod = (365 / 12) * 24 * 60 * 60;
                        break;
                    case 3:
                        // 3 months
                        mPeriod = (365 / 4) * 24 * 60 * 60;
                        break;
                    case 4:
                        // 6 months
                        mPeriod = (365 / 2) * 24 * 60 * 60;
                        break;
                    case 5:
                        // 1 year
                        mPeriod = 365 * 24 * 60 * 60;
                        break;
                    case 6:
                        // all, achieved by setting the the period to the last 50 years
                        mPeriod = 50 * 365 * 24 * 60 * 60;
                        break;
                }
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshData();
    }

    private void updateForwardingEventDisplayList() {

        // Save state, we want to keep the scroll offset after the update.
        Parcelable recyclerViewState;
        recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();

        mForwardingItems.clear();

        List<ForwardingListItem> forwardingEvents = new LinkedList<>();
        Set<ForwardingListItem> dateLines = new HashSet<>();

        long earnedMsats = 0;
        if (mForwardingEventsList != null) {
            // Add all relevant items the forwardingEvents list
            for (ForwardingEvent forwardingEvent : mForwardingEventsList) {
                earnedMsats += forwardingEvent.getFeeMsat();
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

        // Set earned amount texts
        mTVAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(earnedMsats / 1000));
        mTVUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());

        // Restore state (e.g. scroll offset)
        mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

        refreshFinished();
    }

    @Override
    public void onRefresh() {
        refreshData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onForwardingEventSelect(ByteString forwardingEvent) {
        // ToDo: Open details page when a forwarding event was selected.
    }

    private void refreshData() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs() && LndConnection.getInstance().isConnected()) {
            setTitle(getResources().getString(R.string.activity_forwarding));
            mTVAmount.setVisibility(View.GONE);
            mVEarnedAmountProgress.setVisibility(View.VISIBLE);
            mEmptyListText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            fetchForwardingHistory(10000, mPeriod);
        } else {
            refreshFinished();
        }
    }

    private void refreshFinished() {
        mTVAmount.setVisibility(View.VISIBLE);
        mVEarnedAmountProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchForwardingHistory(int pageSize, long timeframe) {
        mTempForwardingEventsList = new LinkedList<>();
        fetchForwardingHistory(pageSize, 0, timeframe);
    }

    private void fetchForwardingHistory(int pageSize, int lastOffset, long timeframe) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        if (LndConnection.getInstance().getLightningService() != null) {
            ForwardingHistoryRequest forwardingHistoryRequest = ForwardingHistoryRequest.newBuilder()
                    .setStartTime((System.currentTimeMillis() / 1000) - timeframe)
                    .setNumMaxEvents(pageSize)
                    .setIndexOffset(lastOffset)
                    .build();

            compositeDisposable.add(LndConnection.getInstance().getLightningService().forwardingHistory(forwardingHistoryRequest)
                    .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                    .subscribe(forwardingResponse -> {
                                if (forwardingResponse.getForwardingEventsList().size() == pageSize) {
                                    // The page is full, save the current list and load the next page
                                    mTempForwardingEventsList.addAll(forwardingResponse.getForwardingEventsList());
                                    fetchForwardingHistory(pageSize, forwardingResponse.getLastOffsetIndex(), timeframe);
                                } else {
                                    mTempForwardingEventsList.addAll(forwardingResponse.getForwardingEventsList());
                                    mForwardingEventsList = mTempForwardingEventsList;
                                    updateForwardingEventDisplayList();
                                    compositeDisposable.dispose();
                                }
                            }
                            , throwable -> {
                                ZapLog.w(LOG_TAG, "Fetching forwarding event list failed." + throwable.getMessage());
                            }));
        }
    }
}
