package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;
import zapsolutions.zap.util.ChannelUtil;

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
    public ByteString getByteString() {
        return ChannelUtil.serialize(mChannel);
    }

    public PendingChannelsResponse.ForceClosedChannel getChannel() {
        return mChannel;
    }

}
