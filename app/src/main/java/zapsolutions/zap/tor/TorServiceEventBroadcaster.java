package zapsolutions.zap.tor;

import androidx.annotation.NonNull;

import io.matthewnelson.topl_service_base.TorPortInfo;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.util.ZapLog;

public class TorServiceEventBroadcaster extends io.matthewnelson.topl_service_base.TorServiceEventBroadcaster {

    private static final String LOG_TAG = "Tor Event";

    @Override
    public void broadcastPortInformation(TorPortInfo torPortInfo) {
        ZapLog.d(LOG_TAG, "PortInfo: " + torPortInfo.getHttpPort());

        if (torPortInfo.getHttpPort() != null) {

            int port = Integer.valueOf(torPortInfo.getHttpPort().split(":")[1]);
            TorManager.getInstance().setIsProxyRunning(true);
            TorManager.getInstance().setProxyPort(port);

            // restart HTTP Client
            HttpClient.getInstance().restartHttpClient();

            // restart LND Connection
            LndConnection.getInstance().reconnect();
        } else {
            TorManager.getInstance().setIsProxyRunning(false);
        }
    }

    @Override
    public void broadcastBandwidth(@NonNull String download, @NonNull String upload) {
        ZapLog.v(LOG_TAG, "bandwidth: " + download + ", " + upload);
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
    public void broadcastLogMessage(String message) {
        ZapLog.d(LOG_TAG, message);
    }

    @Override
    public void broadcastNotice(@NonNull String notice) {
        ZapLog.v(LOG_TAG, notice);
        if (notice.startsWith("WARN|BaseEventListener|Problem bootstrapping.")) {
            TorManager.getInstance().broadcastTorError();
        }
    }

    @Override
    public void broadcastTorState(@NonNull String torState, @NonNull String networkState) {
        ZapLog.d(LOG_TAG, torState + ", " + networkState);
    }
}
