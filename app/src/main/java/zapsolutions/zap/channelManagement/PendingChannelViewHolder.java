package zapsolutions.zap.channelManagement;

import android.content.res.ColorStateList;
import android.view.View;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;

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

    void bindPendingChannelItem(PendingChannelsResponse.PendingChannel pendingChannel) {
        // Set state
        setState();

        // Set balances
        long availableCapacity = pendingChannel.getCapacity();
        setBalances(pendingChannel.getLocalBalance(), pendingChannel.getRemoteBalance(), availableCapacity);

        // Set name
        setName(pendingChannel.getRemoteNodePub());
    }
}
