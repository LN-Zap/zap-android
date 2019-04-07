package ln_zap.zap.channelManagement;

public abstract class ChannelListItem {

    public static final int TYPE_OPEN_CHANNEL = 0;
    public static final int TYPE_PENDING_CHANNEL = 1;
    public static final int TYPE_CLOSED_CHANNEL = 2;

    abstract public int getType();
}
