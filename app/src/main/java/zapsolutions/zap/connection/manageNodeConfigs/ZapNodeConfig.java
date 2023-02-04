package zapsolutions.zap.connection.manageNodeConfigs;

import androidx.annotation.Nullable;

import zapsolutions.zap.connection.BaseNodeConfig;
import zapsolutions.zap.util.RemoteConnectUtil;

public class ZapNodeConfig extends BaseNodeConfig implements Comparable<ZapNodeConfig> {

    public static final String NODE_TYPE_LOCAL = "local";
    public static final String NODE_TYPE_REMOTE = "remote";


    private String id;
    private String alias;
    private String type;
    private String cert;

    public String getId() {
        return this.id;
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

    public boolean isLocal() {
        return this.type.equals(NODE_TYPE_LOCAL);
    }

    public ZapNodeConfig(String id) {
        this.id = id;
    }

    public boolean isTorHostAddress() {
        return RemoteConnectUtil.isTorHostAddress(getHost());
    }

    @Override
    public int compareTo(ZapNodeConfig zapNodeConfig) {
        ZapNodeConfig other = zapNodeConfig;
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
        ZapNodeConfig zapNodeConfig = (ZapNodeConfig) obj;
        return zapNodeConfig.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        } else {
            return this.alias.hashCode();
        }
    }
}
