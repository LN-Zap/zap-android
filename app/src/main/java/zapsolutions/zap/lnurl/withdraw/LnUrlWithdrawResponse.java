package zapsolutions.zap.lnurl.withdraw;

import java.io.Serializable;

import zapsolutions.zap.lnurl.LnUrlResponse;

/**
 * This class helps to work with the received response from a LNURL-withdraw request.
 * <p>
 * Please refer to step 3 in the following reference:
 * https://github.com/btcontract/lnurl-rfc/blob/master/lnurl-withdraw.md
 */
public class LnUrlWithdrawResponse extends LnUrlResponse implements Serializable {

    public static final String ARGS_KEY = "lnurlWithdrawResponse";

    private String k1;
    private String defaultDescription;
    /**
     * In milliSatoshis
     */
    private long maxWithdrawable;
    /**
     * In milliSatoshis
     */
    private long minWithdrawable;


    public String getK1() {
        return k1;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public long getMaxWithdrawable() {
        return maxWithdrawable;
    }

    public long getMinWithdrawable() {
        return minWithdrawable;
    }
}
