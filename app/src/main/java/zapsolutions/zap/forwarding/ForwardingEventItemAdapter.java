package zapsolutions.zap.forwarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.forwarding.listItems.DateItem;
import zapsolutions.zap.forwarding.listItems.DateLineViewHolder;
import zapsolutions.zap.forwarding.listItems.ForwardingEventItemViewHolder;
import zapsolutions.zap.forwarding.listItems.ForwardingEventListItem;
import zapsolutions.zap.forwarding.listItems.ForwardingListItem;


public class ForwardingEventItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ForwardingEventSelectListener mForwardingEventSelectListener;

    private final SortedList<ForwardingListItem> mSortedList = new SortedList<>(ForwardingListItem.class, new SortedList.Callback<ForwardingListItem>() {
        @Override
        public int compare(ForwardingListItem i1, ForwardingListItem i2) {
            return i1.compareTo(i2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(ForwardingListItem oldItem, ForwardingListItem newItem) {
            return oldItem.equalsWithSameContent(newItem);
        }

        @Override
        public boolean areItemsTheSame(ForwardingListItem item1, ForwardingListItem item2) {
            return item1.equals(item2);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    });

    // Construct the adapter with a data list
    public ForwardingEventItemAdapter(ForwardingEventSelectListener forwardingEventSelectListener) {
        mForwardingEventSelectListener = forwardingEventSelectListener;
    }

    public void add(ForwardingListItem item) {
        mSortedList.add(item);
    }

    public void replaceAll(List<ForwardingListItem> items) {
        mSortedList.replaceAll(items);
    }

    @Override
    public int getItemViewType(int position) {
        return mSortedList.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ForwardingListItem.TYPE_DATE:
                View dateView = inflater.inflate(R.layout.list_element_date_line, parent, false);
                return new DateLineViewHolder(dateView);
            case ForwardingListItem.TYPE_FORWARDING_EVENT:
                View forwardingEventItemView = inflater.inflate(R.layout.list_forwarding_item, parent, false);
                return new ForwardingEventItemViewHolder(forwardingEventItemView);
            default:
                throw new IllegalStateException("Unknown forwarding list item type: " + viewType);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case ForwardingListItem.TYPE_DATE:
                zapsolutions.zap.forwarding.listItems.DateLineViewHolder dateHolder = (zapsolutions.zap.forwarding.listItems.DateLineViewHolder) holder;
                DateItem dateItem = (DateItem) mSortedList.get(position);
                dateHolder.bindDateItem(dateItem);
                break;
            case ForwardingListItem.TYPE_FORWARDING_EVENT:
                ForwardingEventItemViewHolder forwardingEventItemViewHolder = (ForwardingEventItemViewHolder) holder;
                ForwardingEventListItem forwardingEventListItem = (ForwardingEventListItem) mSortedList.get(position);
                forwardingEventItemViewHolder.bindForwardingEventListItem(forwardingEventListItem);
                forwardingEventItemViewHolder.addOnForwardingEventSelectListener(mForwardingEventSelectListener);
                break;
            default:
                throw new IllegalStateException("Unknown forwarding list item type: " + type);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
