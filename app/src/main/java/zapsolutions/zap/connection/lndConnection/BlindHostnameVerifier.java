package zapsolutions.zap.connection.lndConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * This HostnameVerifier trust all host names. No verification will take place.
 * In our context we only use it for tor connections and in debug builds to simplify the regtest setup.
 */
public class BlindHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
