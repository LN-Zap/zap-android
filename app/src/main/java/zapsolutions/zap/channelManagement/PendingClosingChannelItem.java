package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;

public class PendingClosingChannelItem extends ChannelListItem {
    private PendingChannelsResponse.ClosedChannel mChannel;

    public PendingClosingChannelItem(PendingChannelsResponse.ClosedChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_PENDING_CLOSING_CHANNEL;
    }

    @Override
    public ByteString getChannelByteString() {
        return mChannel.toByteString();
    }

    public PendingChannelsResponse.ClosedChannel getChannel() {
        return mChannel;
    }
}
