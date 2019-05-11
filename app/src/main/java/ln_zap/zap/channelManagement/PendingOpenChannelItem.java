package ln_zap.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;

public class PendingOpenChannelItem extends ChannelListItem {
    private PendingChannelsResponse.PendingOpenChannel mChannel;

    public PendingOpenChannelItem(PendingChannelsResponse.PendingOpenChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_PENDING_OPEN_CHANNEL;
    }

    public PendingChannelsResponse.PendingOpenChannel getChannel() {
        return mChannel;
    }
}
