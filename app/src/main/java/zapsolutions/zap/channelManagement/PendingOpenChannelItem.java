package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;
import zapsolutions.zap.util.ChannelUtil;

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
    public ByteString getByteString() {
        return ChannelUtil.serialize(mChannel);
    }

    public PendingChannelsResponse.PendingOpenChannel getChannel() {
        return mChannel;
    }
}
