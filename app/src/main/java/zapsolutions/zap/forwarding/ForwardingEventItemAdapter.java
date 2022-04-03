package zapsolutions.zap.forwarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zapsolutions.zap.R;


public class ForwardingEventItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ForwardingEventListItem> mItems;
    private ForwardingEventSelectListener mForwardingEventSelectListener;

    // Construct the adapter with a data list
    public ForwardingEventItemAdapter(List<ForwardingEventListItem> dataset, ForwardingEventSelectListener forwardingEventSelectListener) {
        mItems = dataset;
        mForwardingEventSelectListener = forwardingEventSelectListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View forwardingEventItemView = inflater.inflate(R.layout.list_forwarding_item, parent, false);
        return new ForwardingEventItemViewHolder(forwardingEventItemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ForwardingEventItemViewHolder forwardingEventItemViewHolder = (ForwardingEventItemViewHolder) holder;
        ForwardingEventListItem forwardingEventListItem = mItems.get(position);
        forwardingEventItemViewHolder.bindForwardingEventListItem(forwardingEventListItem);
        forwardingEventItemViewHolder.addOnForwardingEventSelectListener(mForwardingEventSelectListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
