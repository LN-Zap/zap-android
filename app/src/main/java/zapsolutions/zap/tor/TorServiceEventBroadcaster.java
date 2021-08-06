package zapsolutions.zap.tor;

import androidx.annotation.NonNull;

import io.matthewnelson.topl_service_base.TorPortInfo;
import zapsolutions.zap.util.ZapLog;

public class TorServiceEventBroadcaster extends io.matthewnelson.topl_service_base.TorServiceEventBroadcaster {

    private static final String LOG_TAG = "Tor Event";

    @Override
    public void broadcastPortInformation(TorPortInfo torPortInfo) {
        ZapLog.d(LOG_TAG, "Port " + torPortInfo.getHttpPort());

    }

    @Override
    public void broadcastBandwidth(@NonNull String s, @NonNull String s1) {
        ZapLog.v(LOG_TAG, "bandwidth: " + s + ", " + s1);
    }

    @Override
    public void broadcastDebug(@NonNull String s) {
        ZapLog.d(LOG_TAG, "debug: " + s);
    }

    @Override
    public void broadcastException(String s, Exception e) {
        ZapLog.e(LOG_TAG, "exception: " + s + ", " + e.getMessage());
    }

    @Override
    public void broadcastLogMessage(String s) {
        ZapLog.d(LOG_TAG, "log message: " + s);
    }

    @Override
    public void broadcastNotice(@NonNull String s) {
        ZapLog.d(LOG_TAG, "broadcast notice: " + s);
    }

    @Override
    public void broadcastTorState(@NonNull String torState, @NonNull String networkState) {
        ZapLog.d(LOG_TAG, torState + ", " + networkState);
    }
}
