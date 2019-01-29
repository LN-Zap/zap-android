package ln_zap.zap.historyList;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;


public class HistoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HistoryListItem> mItems;

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    // Construct the adapter with a data list
    public HistoryItemAdapter(List<HistoryListItem> dataset) {
        mItems = dataset;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HistoryListItem.TYPE_DATE:
                View dateView = inflater.inflate(R.layout.history_list_element, parent, false);
                return new TransactionViewHolder(dateView);
            case HistoryListItem.TYPE_TRANSACTION:
                View transactionView = inflater.inflate(R.layout.history_list_element, parent, false);
                return new TransactionViewHolder(transactionView);
            default:
                View defaultView = inflater.inflate(R.layout.history_list_element, parent, false);
                return new TransactionViewHolder(defaultView);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case HistoryListItem.TYPE_DATE:
                DateLineViewHolder dateHolder = (DateLineViewHolder) holder;
                break;

            case HistoryListItem.TYPE_TRANSACTION:
                TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
                TransactionItem transactionItem = (TransactionItem) mItems.get(position);
                transactionHolder.bindTransactionItem(transactionItem);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
