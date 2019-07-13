package zapsolutions.zap.connection.parseConnectionData.lndConnect;

import zapsolutions.zap.connection.RemoteConfiguration;

public class LndConnectConfig extends RemoteConfiguration {

    private String cert;

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }
}
