package zapsolutions.zap.channelManagement;

import android.content.res.ColorStateList;
import android.view.View;
import androidx.core.content.ContextCompat;
import zapsolutions.zap.R;

public class OpenChannelViewHolder extends ChannelViewHolder {

    OpenChannelViewHolder(View v) {
        super(v);
    }

    void setState(boolean isActive) {
        if (isActive) {
            mStatus.setText(R.string.channel_state_open);
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.superGreen)));
            mRootView.setAlpha(1f);
        } else {
            mStatus.setText(R.string.channel_state_offline);
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
            mRootView.setAlpha(0.65f);
        }
    }

    void bindOpenChannelItem(final OpenChannelItem openChannelItem) {
        // Set state
        setState(openChannelItem.getChannel().getActive());

        // Set balances
        long availableCapacity = openChannelItem.getChannel().getCapacity() - openChannelItem.getChannel().getCommitFee();
        setBalances(openChannelItem.getChannel().getLocalBalance(), openChannelItem.getChannel().getRemoteBalance(), availableCapacity);

        // Set name
        setName(openChannelItem.getChannel().getRemotePubkey());

        setOnRootViewClickListener(openChannelItem, ChannelListItem.TYPE_OPEN_CHANNEL);
    }
}
