package zapsolutions.zap.transactionHistory.listItems;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class HistoryItemViewHolder extends RecyclerView.ViewHolder {
    protected Context mContext;

    public HistoryItemViewHolder(View v) {
        super(v);
        mContext = v.getContext();
    }

    public void refreshViewHolder() {

    }
}
