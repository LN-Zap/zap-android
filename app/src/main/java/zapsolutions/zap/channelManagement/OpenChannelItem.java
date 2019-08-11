package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.google.protobuf.ByteString;
import zapsolutions.zap.util.ChannelUtil;

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
    public ByteString getByteString() {
        return ChannelUtil.serialize(mChannel);
    }

    public Channel getChannel() {
        return mChannel;
    }
}
