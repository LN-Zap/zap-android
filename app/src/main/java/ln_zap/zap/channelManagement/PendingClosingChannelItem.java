package ln_zap.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;

public class PendingClosingChannelItem extends ChannelListItem {
    private PendingChannelsResponse.ClosedChannel mChannel;

    public PendingClosingChannelItem(PendingChannelsResponse.ClosedChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_PENDING_CLOSING_CHANNEL;
    }

    public PendingChannelsResponse.ClosedChannel getChannel() {
        return mChannel;
    }
}
