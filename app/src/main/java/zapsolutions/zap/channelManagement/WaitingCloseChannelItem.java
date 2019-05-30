package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;

public class WaitingCloseChannelItem extends ChannelListItem {
    private PendingChannelsResponse.WaitingCloseChannel mChannel;

    public WaitingCloseChannelItem(PendingChannelsResponse.WaitingCloseChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_WAITING_CLOSE_CHANNEL;
    }

    public PendingChannelsResponse.WaitingCloseChannel getChannel() {
        return mChannel;
    }
}
