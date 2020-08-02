package zapsolutions.zap.channelManagement;

import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

public abstract class ChannelListItem implements Comparable<ChannelListItem> {

    public static final int TYPE_OPEN_CHANNEL = 0;
    public static final int TYPE_PENDING_OPEN_CHANNEL = 1;
    public static final int TYPE_PENDING_CLOSING_CHANNEL = 2;
    public static final int TYPE_PENDING_FORCE_CLOSING_CHANNEL = 3;
    public static final int TYPE_WAITING_CLOSE_CHANNEL = 4;
    public static final int TYPE_CLOSED_CHANNEL = 5;

    abstract public int getType();

    abstract public ByteString getChannelByteString();

    @Override
    public int compareTo(ChannelListItem channelListItem) {
        ChannelListItem other = channelListItem;

        Long ownCapacity = 0l;
        switch (this.getType()) {
            case TYPE_OPEN_CHANNEL:
                ownCapacity = ((OpenChannelItem) this).getChannel().getCapacity();
                break;
            case TYPE_PENDING_OPEN_CHANNEL:
                ownCapacity = ((PendingOpenChannelItem) this).getChannel().getChannel().getCapacity();
                break;
            case TYPE_PENDING_CLOSING_CHANNEL:
                ownCapacity = ((PendingClosingChannelItem) this).getChannel().getChannel().getCapacity();
                break;
            case TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                ownCapacity = ((PendingForceClosingChannelItem) this).getChannel().getChannel().getCapacity();
                break;
            case TYPE_WAITING_CLOSE_CHANNEL:
                ownCapacity = ((WaitingCloseChannelItem) this).getChannel().getChannel().getCapacity();
                break;
            case TYPE_CLOSED_CHANNEL:
                ownCapacity = 0l;
        }

        Long otherCapacity = 0l;
        switch (other.getType()) {
            case TYPE_OPEN_CHANNEL:
                otherCapacity = ((OpenChannelItem) other).getChannel().getCapacity();
                break;
            case TYPE_PENDING_OPEN_CHANNEL:
                otherCapacity = ((PendingOpenChannelItem) other).getChannel().getChannel().getCapacity();
                break;
            case TYPE_PENDING_CLOSING_CHANNEL:
                otherCapacity = ((PendingClosingChannelItem) other).getChannel().getChannel().getCapacity();
                break;
            case TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                otherCapacity = ((PendingForceClosingChannelItem) other).getChannel().getChannel().getCapacity();
                break;
            case TYPE_WAITING_CLOSE_CHANNEL:
                otherCapacity = ((WaitingCloseChannelItem) other).getChannel().getChannel().getCapacity();
                break;
            case TYPE_CLOSED_CHANNEL:
                otherCapacity = 0l;
        }

        return ownCapacity.compareTo(otherCapacity);
    }

    public boolean isSameChannel(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ChannelListItem channelListItem = (ChannelListItem) obj;
        if (channelListItem.getType() != this.getType()) {
            return false;
        }

        switch (this.getType()) {
            case TYPE_OPEN_CHANNEL:
                return ((OpenChannelItem) this).getChannel().getChanId() == ((OpenChannelItem) channelListItem).getChannel().getChanId();
            case TYPE_PENDING_OPEN_CHANNEL:
                return ((PendingOpenChannelItem) this).getChannel().getChannel().getChannelPoint().equals(((PendingOpenChannelItem) channelListItem).getChannel().getChannel().getChannelPoint());
            case TYPE_PENDING_CLOSING_CHANNEL:
                return ((PendingClosingChannelItem) this).getChannel().getChannel().getChannelPoint().equals(((PendingClosingChannelItem) channelListItem).getChannel().getChannel().getChannelPoint());
            case TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                return ((PendingForceClosingChannelItem) this).getChannel().getChannel().getChannelPoint().equals(((PendingForceClosingChannelItem) channelListItem).getChannel().getChannel().getChannelPoint());
            case TYPE_WAITING_CLOSE_CHANNEL:
                return ((WaitingCloseChannelItem) this).getChannel().getChannel().getChannelPoint().equals(((WaitingCloseChannelItem) channelListItem).getChannel().getChannel().getChannelPoint());
        }

        return channelListItem.getChannelByteString().equals(this.getChannelByteString());
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ChannelListItem channelListItem = (ChannelListItem) obj;
        if (channelListItem.getType() != this.getType()) {
            return false;
        }

        return channelListItem.getChannelByteString().equals(this.getChannelByteString());
    }

    @Override
    public int hashCode() {
        return this.getChannelByteString().hashCode();
    }
}
