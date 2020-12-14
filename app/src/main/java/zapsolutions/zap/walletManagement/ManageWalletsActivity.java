package zapsolutions.zap.walletManagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.setup.SetupActivity;

public class ManageWalletsActivity extends BaseAppCompatActivity {

    public static final String WALLET_ID = "walletUUID";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<WalletConfig> mWalletItems;
    private TextView mEmptyListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallets);

        mRecyclerView = findViewById(R.id.historyList);
        mEmptyListText = findViewById(R.id.listEmpty);

        mWalletItems = new ArrayList<>();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(ManageWalletsActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create and set adapter
        mAdapter = new WalletItemAdapter(mWalletItems);
        mRecyclerView.setAdapter(mAdapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add a new wallet
                Intent intent = new Intent(ManageWalletsActivity.this, SetupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWalletDisplayList();
    }

    private void updateWalletDisplayList() {

        mWalletItems.clear();
        WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
        mWalletItems.addAll(walletConfigsManager.getAllWalletConfigs(false));

        // Show "No wallets" if the list is empty
        if (mWalletItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }

        // Update the list view
        mAdapter.notifyDataSetChanged();
    }

}
