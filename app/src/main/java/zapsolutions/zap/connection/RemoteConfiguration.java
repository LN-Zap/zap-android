package zapsolutions.zap.connection;

/**
 * Base class meant to be extended for more specific remote configurations like
 * - BTCPay Configuration
 * - LndConnect Configuration
 * <p>
 * The macaroon should always be encoded as base16 string (hex)
 */
public abstract class RemoteConfiguration {

    private String host;
    private int port;
    private String macaroon;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMacaroon() {
        return macaroon;
    }

    public void setMacaroon(String macaroon) {
        this.macaroon = macaroon;
    }
}
