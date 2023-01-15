package zapsolutions.zap.coinControl;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.lightningnetwork.lnd.lnrpc.Utxo;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.Wallet;

public class UTXOsActivity extends BaseAppCompatActivity implements UTXOSelectListener, SwipeRefreshLayout.OnRefreshListener, Wallet.UtxoSubscriptionListener {

    private static final String LOG_TAG = UTXOsActivity.class.getName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<UTXOListItem> mUTXOItems;
    private TextView mEmptyListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_utxos);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.seaBlueGradient3));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));

        mRecyclerView = findViewById(R.id.utxoList);
        mEmptyListText = findViewById(R.id.listEmpty);

        Wallet.getInstance().registerUtxoSubscriptionListener(this);

        mUTXOItems = new ArrayList<>();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(UTXOsActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create and set adapter
        mAdapter = new UTXOItemAdapter(mUTXOItems, this);
        mRecyclerView.setAdapter(mAdapter);

        // display current state of the list
        updateUTXOsDisplayList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the list
        Wallet.getInstance().fetchUTXOs();
        Wallet.getInstance().fetchLockedUTXOs();
    }

    private void updateUTXOsDisplayList() {
        mUTXOItems.clear();
        if (Wallet.getInstance().mUTXOsList != null) {
            for (Utxo utxo : Wallet.getInstance().mUTXOsList) {
                UTXOListItem currItem = new UTXOListItem(utxo);
                mUTXOItems.add(currItem);
            }
            // Show "No UTXOs" if the list is empty
            if (mUTXOItems.size() == 0) {
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
    public void onUtxoSelect(ByteString utxo) {
        Bundle bundle = new Bundle();

        if (utxo != null) {
            UTXODetailBSDFragment utxoDetailBSDFragment = new UTXODetailBSDFragment();
            bundle.putSerializable(UTXODetailBSDFragment.ARGS_UTXO, utxo);
            utxoDetailBSDFragment.setArguments(bundle);
            utxoDetailBSDFragment.show(getSupportFragmentManager(), UTXODetailBSDFragment.TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.helpButton) {
            HelpDialogUtil.showDialog(UTXOsActivity.this, R.string.help_dialog_utxos);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs() && LndConnection.getInstance().isConnected()) {
            Wallet.getInstance().fetchUTXOs();
            Wallet.getInstance().fetchLockedUTXOs();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onUtxoListUpdated() {
        updateUTXOsDisplayList();
    }

    @Override
    public void onLockedUtxoListUpdated() {
        updateUTXOsDisplayList();
    }

    @Override
    protected void onDestroy() {
        Wallet.getInstance().unregisterUtxoSubscriptionListener(this);

        super.onDestroy();
    }
}
