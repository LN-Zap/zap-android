package zapsolutions.zap.connection.manageWalletConfigs;

import zapsolutions.zap.connection.RemoteConfiguration;

public class WalletConfig extends RemoteConfiguration {

    private String alias;
    private String type;
    private String cert;


    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCert() {
        return this.cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

}
