package ln_zap.zap.util;

/**
 * This class is used to create fiat currency objects,
 * which hold all information relevant for Zap about that fiat currency.
 */
public class FiatCurrency {

    /**
     * The currency Code. Used as display symbol if Symbol is empty.
     * Example: "USD", "EUR"
     */
    private String mCode;

    /**
     * The exchange rate to BTC.
     * ALWAYS use the rate to BTC here, not to Satoshis for example.
     */
    private double mRate;

    /**
     * The symbol commonly used.
     * Example for USD: $
     */
    private String mSymbol;


    // ToDo: Timestamp    (To prevent the User to initiate a request with old exchange data.)

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
