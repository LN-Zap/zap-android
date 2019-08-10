package zapsolutions.zap.channelManagement;

import android.view.View;
import zapsolutions.zap.R;

public class PendingOpenChannelViewHolder extends PendingChannelViewHolder {

    public PendingOpenChannelViewHolder(View v) {
        super(v);
    }

    @Override
    int getStatusColor() {
        return R.color.lightningOrange;
    }

    @Override
    int getStatusText() {
        return R.string.channel_state_pending_open;
    }
}
