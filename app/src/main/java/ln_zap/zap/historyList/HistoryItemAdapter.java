package ln_zap.zap.historyList;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;

public class HistoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HistoryListItem> mItems;


    // Provide access to all the views for a data item in a view holder
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mAmount;
        public TextView mDescription;
        public TransactionViewHolder(View v) {
            super(v);
            mAmount = v.findViewById(R.id.transactionAmount);
            mDescription = v.findViewById(R.id.transactionDescription);
        }
    }

    // Provide access to all the views for a data item in a view holder
    public static class DateViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public DateViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

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
                DateViewHolder dateHolder = (DateViewHolder) holder;
                break;

            case HistoryListItem.TYPE_TRANSACTION:
                TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
                TransactionItem tItem = (TransactionItem) mItems.get(position);
                Long amt = tItem.getOnChainTransaction().getAmount();
                transactionHolder.mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));

                if (amt>0){
                    transactionHolder.mAmount.setTextColor(Color.GREEN);
                    transactionHolder.mDescription.setText("Empfangen");
                }else{
                    transactionHolder.mAmount.setTextColor(Color.RED);
                }
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
