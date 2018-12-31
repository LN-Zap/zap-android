package ln_zap.zap.util;


public class FiatCurrency {
    private String mCode;
    // Rate to BTC not Satoshi
    private double mRate;
    private String mSymbol;
    // ToDo: Timestamp    (To prevent the User to initiates a request with old exchange data.)

    public FiatCurrency(String code, double rate){
        mCode = code;
        mRate = rate;
    }

    public FiatCurrency(String code, double rate, String symbol){
        mCode = code;
        mRate = rate;
        mSymbol = symbol;
    }

    public String getCode() {
        return mCode;
    }

    public double getRate() {
        return mRate;
    }

    public String getSymbol() {
        return mSymbol;
    }
}
