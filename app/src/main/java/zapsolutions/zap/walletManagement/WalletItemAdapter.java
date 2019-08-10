package zapsolutions.zap.walletManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;

import java.util.List;


public class WalletItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<WalletConfig> mItems;

    // Construct the adapter with a data list
    public WalletItemAdapter(List<WalletConfig> dataset) {
        mItems = dataset;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View walletItemView = inflater.inflate(R.layout.wallet_list_element, parent, false);
        return new WalletItemViewHolder(walletItemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        WalletItemViewHolder walletViewHolder = (WalletItemViewHolder) holder;
        WalletConfig remoteWalletItem = mItems.get(position);
        walletViewHolder.bindRemoteWalletItem(remoteWalletItem);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
