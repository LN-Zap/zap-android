package zapsolutions.zap.channelManagement;

import com.github.lightningnetwork.lnd.lnrpc.Channel;

public class OpenChannelItem extends ChannelListItem {
    private Channel mChannel;

    public OpenChannelItem(Channel channel) {
        mChannel = channel;
    }

    @Override
    public int getType() {
        return TYPE_OPEN_CHANNEL;
    }

    public Channel getChannel() {
        return mChannel;
    }
}
