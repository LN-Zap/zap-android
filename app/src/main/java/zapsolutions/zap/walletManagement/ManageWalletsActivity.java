package zapsolutions.zap.walletManagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.setup.SetupActivity;
import zapsolutions.zap.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class ManageWalletsActivity extends BaseAppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<WalletItem> mWalletItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallets);

        mRecyclerView = findViewById(R.id.historyList);

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

                if (PrefsUtil.isWalletSetup()) {
                    // Add a new wallet
                    Intent intent = new Intent(ManageWalletsActivity.this, SetupActivity.class);
                    intent.putExtra("setupMode", SetupActivity.ADD_WALLET);
                    startActivity(intent);
                } else {
                    // Initiate the setup process
                    Intent intent = new Intent(ManageWalletsActivity.this, SetupActivity.class);
                    intent.putExtra("setupMode", SetupActivity.FULL_SETUP);
                    startActivity(intent);
                }
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


        for (WalletConfig config : walletConfigsManager.getAllWalletConfigs(false)) {
            WalletItem walletListItem = new WalletItem(config);
            mWalletItems.add(walletListItem);
        }


        // Show "No wallets" if the list is empty
        if (mWalletItems.size() == 0) {
            // mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            // mEmptyListText.setVisibility(View.GONE);
        }

        // Update the list view
        mAdapter.notifyDataSetChanged();
    }

}
