package zapsolutions.zap.connection.btcPay;

public class BTCPayConfiguration {
    private String type;
    private String cryptoCode;
    private String host;
    private int port;
    private String macaroon;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCryptoCode() {
        return this.cryptoCode;
    }

    public void setCryptoCode(String cryptoCode) {
        this.cryptoCode = cryptoCode;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMacaroon() {
        return this.macaroon;
    }

    public void setMacaroon(String macaroon) {
        this.macaroon = macaroon;
    }
}
