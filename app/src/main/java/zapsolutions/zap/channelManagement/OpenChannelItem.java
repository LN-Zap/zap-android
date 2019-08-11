package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.google.protobuf.ByteString;

public class OpenChannelItem extends ChannelListItem {
    private Channel mChannel;

    public OpenChannelItem(Channel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_OPEN_CHANNEL;
    }

    @Override
    public ByteString getChannelByteString() {
        return mChannel.toByteString();
    }

    public Channel getChannel() {
        return mChannel;
    }
}
