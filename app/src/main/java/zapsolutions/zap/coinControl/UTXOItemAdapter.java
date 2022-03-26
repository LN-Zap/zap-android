package zapsolutions.zap.coinControl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.transactionHistory.TransactionSelectListener;


public class UTXOItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<UTXOListItem> mItems;
    private UTXOSelectListener mUtxoSelectListener;

    // Construct the adapter with a data list
    public UTXOItemAdapter(List<UTXOListItem> dataset, UTXOSelectListener utxoSelectListener) {
        mItems = dataset;
        mUtxoSelectListener = utxoSelectListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View utxoItemView = inflater.inflate(R.layout.list_utxo_item, parent, false);
        return new UTXOItemViewHolder(utxoItemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        UTXOItemViewHolder utxoItemViewHolder = (UTXOItemViewHolder) holder;
        UTXOListItem utxoListItem = mItems.get(position);
        utxoItemViewHolder.bindUTXOListItem(utxoListItem);
        utxoItemViewHolder.addOnUTXOSelectListener(mUtxoSelectListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
