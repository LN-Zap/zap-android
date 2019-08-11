package zapsolutions.zap.channelManagement;

import android.view.View;
import zapsolutions.zap.R;

public class PendingForceClosingChannelViewHolder extends PendingChannelViewHolder {

    public PendingForceClosingChannelViewHolder(View v) {
        super(v);
    }

    @Override
    int getStatusColor() {
        return R.color.superRed;
    }

    @Override
    int getStatusText() {
        return R.string.channel_state_pending_force_closing;
    }

    void bindPendingForceClosingChannelItem(PendingForceClosingChannelItem pendingForceClosedChannelItem) {
        bindPendingChannelItem(pendingForceClosedChannelItem.getChannel().getChannel());

        setOnRootViewClickListener(pendingForceClosedChannelItem, ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL);
    }
}
