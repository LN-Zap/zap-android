package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;

public class PendingForceClosingChannelItem extends ChannelListItem {
    private PendingChannelsResponse.ForceClosedChannel mChannel;

    public PendingForceClosingChannelItem(PendingChannelsResponse.ForceClosedChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_PENDING_FORCE_CLOSING_CHANNEL;
    }

    @Override
    public ByteString getChannelByteString() {
        return mChannel.toByteString();
    }

    public PendingChannelsResponse.ForceClosedChannel getChannel() {
        return mChannel;
    }

}
