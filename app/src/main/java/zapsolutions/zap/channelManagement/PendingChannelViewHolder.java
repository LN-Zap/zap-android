package zapsolutions.zap.channelManagement;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import zapsolutions.zap.util.OnSingleClickListener;

public abstract class PendingChannelViewHolder extends ChannelViewHolder {

    PendingChannelViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    abstract @ColorRes
    int getStatusColor();

    abstract @StringRes
    int getStatusText();

    private void setState() {
        mStatus.setText(getStatusText());
        mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, getStatusColor())));
        mRootView.setAlpha(0.65f);
    }

    void bindChannelItem(PendingChannelsResponse.PendingChannel pendingChannel) {
        // Set state
        setState();

        // Set balances
        long availableCapacity = pendingChannel.getCapacity();
        setBalances(pendingChannel.getLocalBalance(), pendingChannel.getRemoteBalance(), availableCapacity);

        // Set name
        setName(pendingChannel.getRemoteNodePub());

        // OnClick
        setOnClickListener();
    }

    public void setOnClickListener() {
        // Set on click listener
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(mContext, ChannelDetailsActivity.class);
                mContext.startActivity(intent);
            }
        });
    }
}
