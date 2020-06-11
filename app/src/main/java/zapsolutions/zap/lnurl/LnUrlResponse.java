package zapsolutions.zap.lnurl;

import java.io.Serializable;

/**
 * This abstract class is used as a basis for all responses received from an LNURL service.
 * It contains the common fields across LNURL-Withdraw, LNURL-Pay, etc.
 * Each LNURL response should extend this base class.
 *
 * Please refer to the following reference:
 * https://github.com/btcontract/lnurl-rfc
 */
public abstract class LnUrlResponse implements Serializable {

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

    public boolean hasError(){
        if (status == null){
            return false;
        }
        return status.equals("ERROR");
    }

    public boolean isWithdraw(){
        return tag.equals(LnUrlResponse.TAG_WITHDRAW);
    }
}
