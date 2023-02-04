package zapsolutions.zap.connection;

/**
 * Base class meant to be extended for more specific remote configurations like
 * - BTCPay Configuration
 * - LndConnect Configuration
 * <p>
 * The macaroon should always be encoded as base16 string (hex)
 */
public abstract class BaseNodeConfig {

    public static final String NODE_IMPLEMENTATION_LND = "lnd";
    public static final String NODE_IMPLEMENTATION_ECLAIR = "eclair";
    public static final String NODE_IMPLEMENTATION_CLIGHTNING = "c-lightning";

    private String host;
    private int port;
    private String macaroon;
    private String implementation;
    private boolean UseTor;
    private boolean VerifyCertificate;

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

    public String getImplementation() {
        return this.implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public boolean getUseTor() {
        return this.UseTor;
    }

    public void setUseTor(boolean useTor) {
        this.UseTor = useTor;
    }

    public boolean getVerifyCertificate() {
        return this.VerifyCertificate;
    }

    public void setVerifyCertificate(boolean verifyCertificate) {
        this.VerifyCertificate = verifyCertificate;
    }
}
