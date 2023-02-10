package zapsolutions.zap.connection.parseConnectionData.btcPay;

import zapsolutions.zap.connection.BaseNodeConfig;

public class BTCPayConfig extends BaseNodeConfig {

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
