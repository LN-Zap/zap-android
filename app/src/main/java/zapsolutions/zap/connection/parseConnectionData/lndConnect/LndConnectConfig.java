package zapsolutions.zap.connection.parseConnectionData.lndConnect;

import zapsolutions.zap.connection.BaseNodeConfig;

public class LndConnectConfig extends BaseNodeConfig {

    private String cert;

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }
}
