package zapsolutions.zap.channelManagement;

import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.util.Wallet;

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

        String ownPubkey = "";
        switch (this.getType()) {
            case TYPE_OPEN_CHANNEL:
                ownPubkey = ((OpenChannelItem) this).getChannel().getRemotePubkey();
                break;
            case TYPE_PENDING_OPEN_CHANNEL:
                ownPubkey = ((PendingOpenChannelItem) this).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_PENDING_CLOSING_CHANNEL:
                ownPubkey = ((PendingClosingChannelItem) this).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                ownPubkey = ((PendingForceClosingChannelItem) this).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_WAITING_CLOSE_CHANNEL:
                ownPubkey = ((WaitingCloseChannelItem) this).getChannel().getChannel().getRemoteNodePub();
        }

        String otherPubkey = "";
        switch (other.getType()) {
            case TYPE_OPEN_CHANNEL:
                otherPubkey = ((OpenChannelItem) other).getChannel().getRemotePubkey();
                break;
            case TYPE_PENDING_OPEN_CHANNEL:
                otherPubkey = ((PendingOpenChannelItem) other).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_PENDING_CLOSING_CHANNEL:
                otherPubkey = ((PendingClosingChannelItem) other).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                otherPubkey = ((PendingForceClosingChannelItem) other).getChannel().getChannel().getRemoteNodePub();
                break;
            case TYPE_WAITING_CLOSE_CHANNEL:
                otherPubkey = ((WaitingCloseChannelItem) other).getChannel().getChannel().getRemoteNodePub();
        }

        String ownAlias = Wallet.getInstance().getNodeAliasFromPubKey(ownPubkey, App.getAppContext()).toLowerCase();
        String otherAlias = Wallet.getInstance().getNodeAliasFromPubKey(otherPubkey, App.getAppContext()).toLowerCase();

        return ownAlias.compareTo(otherAlias);
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
