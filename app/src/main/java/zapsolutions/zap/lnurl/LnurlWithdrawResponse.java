package zapsolutions.zap.lnurl;

import java.io.Serializable;

public class LnurlWithdrawResponse extends LnurlResponse implements Serializable {

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
