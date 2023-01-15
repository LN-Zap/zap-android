package zapsolutions.zap.nodesManagement;

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
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.setup.SetupActivity;

public class ManageNodesActivity extends BaseAppCompatActivity {

    public static final String NODE_ID = "nodeUUID";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<NodeConfig> mNodeItems;
    private TextView mEmptyListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_nodes);

        mRecyclerView = findViewById(R.id.nodesList);
        mEmptyListText = findViewById(R.id.listEmpty);

        mNodeItems = new ArrayList<>();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(ManageNodesActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create and set adapter
        mAdapter = new NodeItemAdapter(mNodeItems);
        mRecyclerView.setAdapter(mAdapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add a new node
                Intent intent = new Intent(ManageNodesActivity.this, SetupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNodeDisplayList();
    }

    private void updateNodeDisplayList() {

        mNodeItems.clear();
        NodeConfigsManager nodeConfigsManager = NodeConfigsManager.getInstance();
        mNodeItems.addAll(nodeConfigsManager.getAllNodeConfigs(false));

        // Show "No nodes" if the list is empty
        if (mNodeItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }

        // Update the list view
        mAdapter.notifyDataSetChanged();
    }

}
