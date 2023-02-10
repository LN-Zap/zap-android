package zapsolutions.zap.nodesManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;


public class NodeItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ZapNodeConfig> mItems;

    // Construct the adapter with a data list
    public NodeItemAdapter(List<ZapNodeConfig> dataset) {
        mItems = dataset;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View nodeItemView = inflater.inflate(R.layout.node_list_element, parent, false);
        return new NodeItemViewHolder(nodeItemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        NodeItemViewHolder nodeViewHolder = (NodeItemViewHolder) holder;
        ZapNodeConfig remoteNodeItem = mItems.get(position);
        nodeViewHolder.bindRemoteNodeItem(remoteNodeItem);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
