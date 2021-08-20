package zapsolutions.zap.tor;

import java.util.HashSet;
import java.util.Set;

import io.matthewnelson.topl_service.TorServiceController;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;

/**
 * Singleton to manage Tor.
 */
public class TorManager {
    private static TorManager mTorManagerInstance;
    private static final String LOG_TAG = TorManager.class.getName();

    private final Set<TorErrorListener> mTorErrorListeners = new HashSet<>();

    private int mProxyPort;
    private boolean isProxyRunning = false;

    public int getProxyPort() {
        return mProxyPort;
    }

    public void setProxyPort(int mProxyPort) {
        this.mProxyPort = mProxyPort;
    }

    public boolean isProxyRunning() {
        return isProxyRunning;
    }

    public void setIsProxyRunning(boolean proxyRunning) {
        isProxyRunning = proxyRunning;
    }

    private TorManager() {
    }

    public void startTor() {
        TorServiceController.startTor();
    }

    public void stopTor() {
        TorServiceController.stopTor();
    }

    public void restartTor() {
        TorServiceController.restartTor();
    }

    public void switchTorState(boolean newActive) {
        if (newActive) {
            startTor();
            // restarting HTTP Client and LND Connection will happen automatically as soon as the new proxy is established.
        } else {
            stopTor();

            // restart HTTP Client
            HttpClient.getInstance().restartHttpClient();

            // restart LND Connection
            LndConnection.getInstance().reconnect();
        }
    }

    public boolean isCurrentConnectionTor() {
        if (LndConnection.getInstance().getConnectionConfig() != null) {
            if (LndConnection.getInstance().getConnectionConfig().isLocal()) {
                return false;
            } else {
                return LndConnection.getInstance().getConnectionConfig().isTor();
            }
        } else {
            return false;
        }
    }

    public int getTorTimeoutMultiplier() {
        if (PrefsUtil.isTorEnabled() || isCurrentConnectionTor()) {
            return RefConstants.TOR_TIMEOUT_MULTIPLIER;
        } else {
            return 1;
        }
    }

    public static synchronized TorManager getInstance() {
        if (mTorManagerInstance == null) {
            mTorManagerInstance = new TorManager();
        }
        return mTorManagerInstance;
    }

    public void broadcastTorError() {
        for (TorErrorListener listener : mTorErrorListeners) {
            listener.onTorBootstrappingFailed();
        }
    }

    public void registerTorErrorListener(TorErrorListener listener) {
        mTorErrorListeners.add(listener);
    }

    public void unregisterTorErrorListener(TorErrorListener listener) {
        mTorErrorListeners.remove(listener);
    }

    public interface TorErrorListener {
        void onTorBootstrappingFailed();
    }
}
