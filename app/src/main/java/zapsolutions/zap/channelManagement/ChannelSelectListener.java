package zapsolutions.zap.channelManagement;

import com.google.protobuf.ByteString;

public interface ChannelSelectListener {

    void onChannelSelect(ByteString channel, int type);
}
