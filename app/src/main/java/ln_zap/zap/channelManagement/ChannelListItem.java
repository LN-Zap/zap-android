package ln_zap.zap.channelManagement;

public abstract class ChannelListItem {

    public static final int TYPE_OPEN_CHANNEL = 0;
    public static final int TYPE_PENDING_OPEN_CHANNEL = 1;
    public static final int TYPE_PENDING_CLOSING_CHANNEL = 2;
    public static final int TYPE_PENDING_FORCE_CLOSING_CHANNEL = 3;
    public static final int TYPE_WAITING_CLOSE_CHANNEL = 4;
    public static final int TYPE_CLOSED_CHANNEL = 5;

    abstract public int getType();
}
