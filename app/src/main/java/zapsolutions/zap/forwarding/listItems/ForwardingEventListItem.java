package zapsolutions.zap.forwarding.listItems;

import com.github.lightningnetwork.lnd.lnrpc.ForwardingEvent;

public class ForwardingEventListItem extends ForwardingListItem {


    private final ForwardingEvent mForwardingEvent;

    public ForwardingEventListItem(ForwardingEvent forwardingEvent) {
        mForwardingEvent = forwardingEvent;
        mTimestamp = forwardingEvent.getTimestampNs();
    }

    public ForwardingEvent getForwardingEvent() {
        return mForwardingEvent;
    }

    @Override
    public int getType() {
        return TYPE_FORWARDING_EVENT;
    }
}
