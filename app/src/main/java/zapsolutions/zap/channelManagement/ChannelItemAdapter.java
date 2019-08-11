package zapsolutions.zap.channelManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;

import java.util.List;


public class ChannelItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChannelListItem> mItems;
    private ChannelSelectListener mChannelSelectListener;

    // Construct the adapter with a data list
    public ChannelItemAdapter(List<ChannelListItem> dataset, ChannelSelectListener channelSelectListener) {
        mItems = dataset;
        mChannelSelectListener = channelSelectListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View channelView = inflater.inflate(R.layout.channel_list_element_channel, parent, false);

        switch (viewType) {
            case ChannelListItem.TYPE_OPEN_CHANNEL:
                return new OpenChannelViewHolder(channelView);
            case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                return new PendingOpenChannelViewHolder(channelView);
            case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                return new PendingClosingChannelViewHolder(channelView);
            case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                return new PendingForceClosingChannelViewHolder(channelView);
            case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                return new WaitingCloseChannelViewHolder(channelView);
            default:
                throw new IllegalStateException("Unknown channel type: " + viewType);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);

        switch (type) {
            case ChannelListItem.TYPE_OPEN_CHANNEL:
                OpenChannelViewHolder openChannelHolder = (OpenChannelViewHolder) holder;
                OpenChannelItem openChannelItem = (OpenChannelItem) mItems.get(position);
                openChannelHolder.bindOpenChannelItem(openChannelItem);
                openChannelHolder.addOnChannelSelectListener(mChannelSelectListener);
                break;
            case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                PendingOpenChannelViewHolder pendingOpenChannelHolder = (PendingOpenChannelViewHolder) holder;
                PendingOpenChannelItem pendingOpenChannelItem = (PendingOpenChannelItem) mItems.get(position);
                pendingOpenChannelHolder.bindPendingOpenChannelItem(pendingOpenChannelItem);
                pendingOpenChannelHolder.addOnChannelSelectListener(mChannelSelectListener);
                break;
            case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                PendingClosingChannelViewHolder pendingClosingChannelHolder = (PendingClosingChannelViewHolder) holder;
                PendingClosingChannelItem pendingClosingChannelItem = (PendingClosingChannelItem) mItems.get(position);
                pendingClosingChannelHolder.bindPendingClosingChannelItem(pendingClosingChannelItem);
                pendingClosingChannelHolder.addOnChannelSelectListener(mChannelSelectListener);
                break;
            case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                PendingForceClosingChannelViewHolder pendingForceClosingChannelHolder = (PendingForceClosingChannelViewHolder) holder;
                PendingForceClosingChannelItem pendingForceClosingChannelItem = (PendingForceClosingChannelItem) mItems.get(position);
                pendingForceClosingChannelHolder.bindPendingForceClosingChannelItem(pendingForceClosingChannelItem);
                pendingForceClosingChannelHolder.addOnChannelSelectListener(mChannelSelectListener);
                break;
            case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                WaitingCloseChannelViewHolder waitingCloseChannelHolder = (WaitingCloseChannelViewHolder) holder;
                WaitingCloseChannelItem waitingCloseChannelItem = (WaitingCloseChannelItem) mItems.get(position);
                waitingCloseChannelHolder.bindWaitingClosedChannelItem(waitingCloseChannelItem);
                waitingCloseChannelHolder.addOnChannelSelectListener(mChannelSelectListener);
                break;
            default:
                throw new IllegalStateException("Unknown channel type: " + type);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
