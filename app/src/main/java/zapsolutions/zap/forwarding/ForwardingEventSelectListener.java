package zapsolutions.zap.forwarding;

import com.google.protobuf.ByteString;

public interface ForwardingEventSelectListener {
    void onForwardingEventSelect(ByteString forwardingEvent);
}
