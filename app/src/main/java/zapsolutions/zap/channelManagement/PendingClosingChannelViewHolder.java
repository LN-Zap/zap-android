package zapsolutions.zap.channelManagement;

import android.view.View;
import androidx.annotation.NonNull;
import zapsolutions.zap.R;

public class PendingClosingChannelViewHolder extends PendingChannelViewHolder {

    public PendingClosingChannelViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    int getStatusColor() {
        return R.color.superRed;
    }

    @Override
    int getStatusText() {
        return R.string.channel_state_pending_closing;
    }
}
