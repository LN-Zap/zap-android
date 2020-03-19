package zapsolutions.zap.connection.manageWalletConfigs;

import androidx.annotation.Nullable;

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

    public WalletConfig (String id) {
        this.id = id;
    }

    @Override
    public int compareTo(WalletConfig walletConfig) {
        WalletConfig other = walletConfig;
        return this.getAlias().compareTo(other.getAlias());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        WalletConfig walletConfig = (WalletConfig) obj;
        return walletConfig.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
