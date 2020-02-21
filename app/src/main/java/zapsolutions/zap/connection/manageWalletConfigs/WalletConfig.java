package zapsolutions.zap.connection.manageWalletConfigs;

import zapsolutions.zap.connection.RemoteConfiguration;

public class WalletConfig extends RemoteConfiguration implements Comparable<WalletConfig> {

    public static final String WALLET_TYPE_LOCAL = "local";
    public static final String WALLET_TYPE_REMOTE = "remote";

    private String id;
    private String alias;
    private String type;
    private String cert;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isLocal() {return this.type.equals(WALLET_TYPE_LOCAL);}

    @Override
    public int compareTo(WalletConfig walletConfig) {
        WalletConfig other = walletConfig;
        return this.getAlias().compareTo(other.getAlias());
    }
}
