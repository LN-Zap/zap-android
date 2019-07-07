package zapsolutions.zap.connection.btcPay;

import zapsolutions.zap.connection.RemoteConfiguration;

public class BTCPayConfig extends RemoteConfiguration {

    public static String NO_CERT = "NO_CERT";
    public static String TYPE_GRPC = "GRPC";
    public static String CRYPTO_TYPE_BTC = "BTC";

    private String type;
    private String cryptoCode;

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
}
