package zapsolutions.zap.lnurl;

import java.io.Serializable;

/**
 * This abstract class is used as a basis for all responses received from an LNURL service.
 * It contains the common fields across LNURL-Withdraw, LNURL-Pay, etc.
 * Each LNURL response should extend this base class.
 * <p>
 * Please refer to the following reference:
 * https://github.com/btcontract/lnurl-rfc
 */
public class LnUrlResponse implements Serializable {

    public static final String TAG_WITHDRAW = "withdrawRequest";
    public static final String TAG_PAY_REQUEST = "payRequest";
    public static final String TAG_CHANNEL_REQUEST = "channelRequest";
    public static final String TAG_HOSTED_CHANNEL_REQUEST = "hostedChannelRequest";

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

    public boolean hasError() {
        if (status == null) {
            return false;
        }
        return status.toLowerCase().equals("error");
    }

    public boolean isWithdraw() {
        return tag.equals(LnUrlResponse.TAG_WITHDRAW);
    }

    public boolean isPayRequest() {
        return tag.equals(LnUrlResponse.TAG_PAY_REQUEST);
    }

    public boolean isChannelRequest() {
        return tag.equals(LnUrlResponse.TAG_CHANNEL_REQUEST);
    }

    public boolean isHostedChannelRequest() {
        return tag.equals(LnUrlResponse.TAG_HOSTED_CHANNEL_REQUEST);
    }
}
