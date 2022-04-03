package zapsolutions.zap.forwarding;

import com.github.lightningnetwork.lnd.lnrpc.ForwardingEvent;

public class ForwardingEventListItem implements Comparable<ForwardingEventListItem> {

    private final long mTimestamp;
    private final ForwardingEvent mForwardingEvent;

    public ForwardingEventListItem(ForwardingEvent forwardingEvent) {
        mForwardingEvent = forwardingEvent;
        mTimestamp = forwardingEvent.getTimestampNs();
    }

    public ForwardingEvent getForwardingEvent() {
        return mForwardingEvent;
    }

    public long getTimestamp() {
        return mTimestamp;
    }


    @Override
    public int compareTo(ForwardingEventListItem o) {
        return Long.compare(o.getTimestamp(), this.mTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForwardingEventListItem that = (ForwardingEventListItem) o;

        return mTimestamp == that.getTimestamp();
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mTimestamp).hashCode();
    }
}
