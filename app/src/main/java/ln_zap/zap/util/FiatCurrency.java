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

    /**
     * Time of the exchange rate data (in seconds since 00:00:00 UTC on January 1, 1970)
     * This is used to protect the User from initiate an "invoice" with old exchange data.
     */
    private long mTimestamp;

    public FiatCurrency(String code, double rate, long timestamp){
        mCode = code;
        mRate = rate;
        mTimestamp = timestamp;
    }

    public FiatCurrency(String code, double rate, long timestamp, String symbol){
        mCode = code;
        mRate = rate;
        mTimestamp = timestamp;
        mSymbol = symbol;
    }

    public String getCode() {
        return mCode;
    }

    public double getRate() {
        return mRate;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getSymbol() {
        return mSymbol;
    }
}
