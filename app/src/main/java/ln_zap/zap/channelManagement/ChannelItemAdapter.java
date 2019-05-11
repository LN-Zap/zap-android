package ln_zap.zap.channelManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.historyList.LnInvoiceItem;
import ln_zap.zap.historyList.LnInvoiceViewHolder;
import ln_zap.zap.historyList.LnPaymentItem;
import ln_zap.zap.historyList.LnPaymentViewHolder;


public class ChannelItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChannelListItem> mItems;

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    // Construct the adapter with a data list
    public ChannelItemAdapter(List<ChannelListItem> dataset) {
        mItems = dataset;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ChannelListItem.TYPE_OPEN_CHANNEL:
                View openChannelView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new OpenChannelViewHolder(openChannelView);
            case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                View pendingOpenChannelView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new PendingOpenChannelViewHolder(pendingOpenChannelView);
            case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                View pendingClosingChannelView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new PendingClosingChannelViewHolder(pendingClosingChannelView);
            case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                View pendingForceClosingChannelView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new PendingForceClosingChannelViewHolder(pendingForceClosingChannelView);
            case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                View waitingCloseChannelView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new WaitingCloseChannelViewHolder(waitingCloseChannelView);
            default:
                View defaultView = inflater.inflate(R.layout.channel_list_element_open_channel, parent, false);
                return new OpenChannelViewHolder(defaultView);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case ChannelListItem.TYPE_OPEN_CHANNEL:
                OpenChannelViewHolder openChannelHolder = (OpenChannelViewHolder) holder;
                OpenChannelItem openChannelItem = (OpenChannelItem) mItems.get(position);
                openChannelHolder.bindOpenChannelItem(openChannelItem);
                break;
            case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                PendingOpenChannelViewHolder pendingOpenChannelHolder = (PendingOpenChannelViewHolder) holder;
                PendingOpenChannelItem pendingOpenChannelItem = (PendingOpenChannelItem) mItems.get(position);
                pendingOpenChannelHolder.bindPendingOpenChannelItem(pendingOpenChannelItem);
                break;
            case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                PendingClosingChannelViewHolder pendingClosingChannelHolder = (PendingClosingChannelViewHolder) holder;
                PendingClosingChannelItem pendingClosingChannelItem = (PendingClosingChannelItem) mItems.get(position);
                pendingClosingChannelHolder.bindPendingClosingChannelItem(pendingClosingChannelItem);
                break;
            case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                PendingForceClosingChannelViewHolder pendingForceClosingChannelHolder = (PendingForceClosingChannelViewHolder) holder;
                PendingForceClosingChannelItem pendingForceClosingChannelItem = (PendingForceClosingChannelItem) mItems.get(position);
                pendingForceClosingChannelHolder.bindPendingForceClosingChannelItem(pendingForceClosingChannelItem);
                break;
            case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                WaitingCloseChannelViewHolder waitingCloseChannelHolder = (WaitingCloseChannelViewHolder) holder;
                WaitingCloseChannelItem waitingCloseChannelItem = (WaitingCloseChannelItem) mItems.get(position);
                waitingCloseChannelHolder.bindWaitingCloseChannelItem(waitingCloseChannelItem);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
