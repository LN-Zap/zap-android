package zapsolutions.zap.nodesManagement;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfig;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;

public class NodeItemViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = NodeItemViewHolder.class.getName();

    private ImageView mIcon;
    private TextView mNodeTypDescription;
    private TextView mNodeName;
    private View mRootView;
    private Context mContext;
    private ImageView mCurrentActiveIcon;

    public NodeItemViewHolder(View v) {
        super(v);

        mIcon = v.findViewById(R.id.nodeTypeIcon);
        mNodeTypDescription = v.findViewById(R.id.nodeTypeDescription);
        mNodeName = v.findViewById(R.id.nodeName);
        mRootView = v.findViewById(R.id.transactionRootView);
        mCurrentActiveIcon = v.findViewById(R.id.currentlyActiveIcon);
        mContext = v.getContext();
    }

    public void bindRemoteNodeItem(NodeConfig nodeConfig) {

        // Set Icon
        if (nodeConfig.getType().equals(NodeConfig.NODE_TYPE_LOCAL)) {
            mIcon.setImageResource(R.drawable.ic_local_black_24dp);
        } else {
            mIcon.setImageResource(R.drawable.ic_remote_black_24dp);
        }
        mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));

        // Set current active icon visibility
        if (nodeConfig.getId().equals(PrefsUtil.getCurrentNodeConfig())) {
            mCurrentActiveIcon.setVisibility(View.VISIBLE);
        } else {
            mCurrentActiveIcon.setVisibility(View.GONE);
        }

        // Set node type description
        mNodeTypDescription.setText(nodeConfig.getType());

        // Set node name
        mNodeName.setText(nodeConfig.getAlias());

        // Set on click listener
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(mContext, NodeDetailsActivity.class);
                intent.putExtra(ManageNodesActivity.NODE_ID, nodeConfig.getId());
                mContext.startActivity(intent);
            }
        });
    }
}
