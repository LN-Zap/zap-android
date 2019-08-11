package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;

public class PendingOpenChannelItem extends ChannelListItem {
    private PendingChannelsResponse.PendingOpenChannel mChannel;

    public PendingOpenChannelItem(PendingChannelsResponse.PendingOpenChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_PENDING_OPEN_CHANNEL;
    }

    @Override
    public ByteString getChannelByteString() {
        return mChannel.toByteString();
    }

    public PendingChannelsResponse.PendingOpenChannel getChannel() {
        return mChannel;
    }
}
