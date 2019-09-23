package zapsolutions.zap.lightning;

import androidx.annotation.NonNull;

public class LightningParser {

    private static int NODE_URI_MIN_LENGTH = 66;

    public static LightningNodeUri parseNodeUri(@NonNull String uri) {
        if (uri.isEmpty() || uri.length() < NODE_URI_MIN_LENGTH) {
            return null;
        }

        if (uri.length() == NODE_URI_MIN_LENGTH) {
            // PubKey only
            return new LightningNodeUri.Builder().setPubKey(uri).build();
        }

        if (!uri.contains("@")) {
            // longer and no @, something is wrong
            return null;
        }

        String[] parts = uri.split("@");

        if (parts.length != 2) {
            return null;
        }

        return new LightningNodeUri.Builder().setPubKey(parts[0]).setHost(parts[1]).build();
    }
}
