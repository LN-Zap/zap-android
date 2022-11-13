package zapsolutions.zap.forwarding.listItems;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ForwardingItemViewHolder extends RecyclerView.ViewHolder {
    protected Context mContext;

    public ForwardingItemViewHolder(View v) {
        super(v);
        mContext = v.getContext();
    }
}
