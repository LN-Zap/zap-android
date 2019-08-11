package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;

public class WaitingCloseChannelItem extends ChannelListItem {
    private PendingChannelsResponse.WaitingCloseChannel mChannel;

    public WaitingCloseChannelItem(PendingChannelsResponse.WaitingCloseChannel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_WAITING_CLOSE_CHANNEL;
    }

    @Override
    public ByteString getChannelByteString() {
        return mChannel.toByteString();
    }

    public PendingChannelsResponse.WaitingCloseChannel getChannel() {
        return mChannel;
    }
}
