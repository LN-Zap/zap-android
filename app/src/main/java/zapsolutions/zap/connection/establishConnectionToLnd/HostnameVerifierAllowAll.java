package zapsolutions.zap.connection.establishConnectionToLnd;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HostnameVerifierAllowAll implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
