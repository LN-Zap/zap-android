package zapsolutions.zap.transactionHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;
import zapsolutions.zap.transactionHistory.listItems.DateItem;
import zapsolutions.zap.transactionHistory.listItems.DateLineViewHolder;
import zapsolutions.zap.transactionHistory.listItems.DemoItem;
import zapsolutions.zap.transactionHistory.listItems.DemoViewHolder;
import zapsolutions.zap.transactionHistory.listItems.HistoryListItem;
import zapsolutions.zap.transactionHistory.listItems.LnInvoiceItem;
import zapsolutions.zap.transactionHistory.listItems.LnInvoiceViewHolder;
import zapsolutions.zap.transactionHistory.listItems.LnPaymentItem;
import zapsolutions.zap.transactionHistory.listItems.LnPaymentViewHolder;
import zapsolutions.zap.transactionHistory.listItems.OnChainTransactionItem;
import zapsolutions.zap.transactionHistory.listItems.OnChainTransactionViewHolder;

import java.util.List;


public class HistoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HistoryListItem> mItems;
    private TransactionSelectListener mTransactionSelectListener;

    // Construct the adapter with a data list
    public HistoryItemAdapter(List<HistoryListItem> dataset, TransactionSelectListener transactionSelectListener) {
        mItems = dataset;
        mTransactionSelectListener = transactionSelectListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HistoryListItem.TYPE_DATE:
                View dateView = inflater.inflate(R.layout.history_list_element_date_line, parent, false);
                return new DateLineViewHolder(dateView);
            case HistoryListItem.TYPE_ON_CHAIN_TRANSACTION:
                View transactionView = inflater.inflate(R.layout.history_list_element_transaction, parent, false);
                return new OnChainTransactionViewHolder(transactionView);
            case HistoryListItem.TYPE_LN_INVOICE:
                View lnTransactionView = inflater.inflate(R.layout.history_list_element_transaction, parent, false);
                return new LnInvoiceViewHolder(lnTransactionView);
            case HistoryListItem.TYPE_LN_PAYMENT:
                View lnPaymentView = inflater.inflate(R.layout.history_list_element_transaction, parent, false);
                return new LnPaymentViewHolder(lnPaymentView);
            case HistoryListItem.TYPE_DEMO:
                View demoView = inflater.inflate(R.layout.history_list_element_transaction, parent, false);
                return new DemoViewHolder(demoView);
            default:
                throw new IllegalStateException("Unknown history list item type: " + viewType);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case HistoryListItem.TYPE_DATE:
                DateLineViewHolder dateHolder = (DateLineViewHolder) holder;
                DateItem dateItem = (DateItem) mItems.get(position);
                dateHolder.bindDateItem(dateItem);
                break;
            case HistoryListItem.TYPE_ON_CHAIN_TRANSACTION:
                OnChainTransactionViewHolder onChainTransactionHolder = (OnChainTransactionViewHolder) holder;
                OnChainTransactionItem onChainTransactionItem = (OnChainTransactionItem) mItems.get(position);
                onChainTransactionHolder.bindOnChainTransactionItem(onChainTransactionItem);
                onChainTransactionHolder.addOnTransactionSelectListener(mTransactionSelectListener);
                break;
            case HistoryListItem.TYPE_LN_INVOICE:
                LnInvoiceViewHolder lnInvoiceHolder = (LnInvoiceViewHolder) holder;
                LnInvoiceItem lnInvoiceItem = (LnInvoiceItem) mItems.get(position);
                lnInvoiceHolder.bindLnInvoiceItem(lnInvoiceItem);
                lnInvoiceHolder.addOnTransactionSelectListener(mTransactionSelectListener);
                break;
            case HistoryListItem.TYPE_LN_PAYMENT:
                LnPaymentViewHolder lnPaymentHolder = (LnPaymentViewHolder) holder;
                LnPaymentItem lnPaymentItem = (LnPaymentItem) mItems.get(position);
                lnPaymentHolder.bindLnPaymentItem(lnPaymentItem);
                lnPaymentHolder.addOnTransactionSelectListener(mTransactionSelectListener);
                break;
            case HistoryListItem.TYPE_DEMO:
                DemoViewHolder demoHolder = (DemoViewHolder) holder;
                DemoItem demoItem = (DemoItem) mItems.get(position);
                demoHolder.bindDemoItem(demoItem);
                demoHolder.addOnTransactionSelectListener(mTransactionSelectListener);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
