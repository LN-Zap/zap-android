package zapsolutions.zap.lnurl;

import java.io.Serializable;

public abstract class LnurlResponse implements Serializable {

    public static final String TAG_WITHDRAW = "withdrawRequest";

    private String tag;
    private String callback;
    private String status;
    private String reason;


    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getTag() {
        return tag;
    }

    public String getCallback() {
        return callback;
    }
}
