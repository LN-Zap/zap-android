package zapsolutions.zap.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import java.io.IOException;

public class ChannelUtil {

    public static ByteString serialize(MessageLite channel) {
        ByteString.Output output = ByteString.newOutput(channel.getSerializedSize());
        try {
            channel.writeTo(output);
            return output.toByteString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
