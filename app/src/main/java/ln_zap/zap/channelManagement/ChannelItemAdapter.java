package ln_zap.zap.channelManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;


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
                /*
            case ChannelListItem.TYPE_PENDING_CHANNEL:
                View pendingChannelView = inflater.inflate(R.layout.history_list_element_ln_invoice, parent, false);
                return new LnInvoiceViewHolder(pendingChannelView);
            case ChannelListItem.TYPE_CLOSED_CHANNEL:
                View closedChannelView = inflater.inflate(R.layout.history_list_element_ln_payment, parent, false);
                return new LnPaymentViewHolder(closedChannelView);
                */
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
            /*
            case ChannelListItem.TYPE_LN_INVOICE:
                LnInvoiceViewHolder lnInvoiceHolder = (LnInvoiceViewHolder) holder;
                LnInvoiceItem lnInvoiceItem = (LnInvoiceItem) mItems.get(position);
                lnInvoiceHolder.bindLnInvoiceItem(lnInvoiceItem);
                break;

            case ChannelListItem.TYPE_LN_PAYMENT:
                LnPaymentViewHolder lnPaymentHolder = (LnPaymentViewHolder) holder;
                LnPaymentItem lnPaymentItem = (LnPaymentItem) mItems.get(position);
                lnPaymentHolder.bindLnPaymentItem(lnPaymentItem);
                break;
                */
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
