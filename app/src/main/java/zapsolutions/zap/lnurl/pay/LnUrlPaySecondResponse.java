package zapsolutions.zap.lnurl.pay;

import java.io.Serializable;

import zapsolutions.zap.lnurl.LnUrlResponse;

/**
 * This class helps to work with the received response from a LNURL-pay request.
 * <p>
 * Please refer to step 6 in the following reference:
 * https://github.com/btcontract/lnurl-rfc/blob/master/lnurl-pay.md
 */
public class LnUrlPaySecondResponse extends LnUrlResponse implements Serializable {

    private String pr;

    private boolean disposable;

    private LnUrlPaySuccessAction successAction;

    public String getPaymentRequest() {
        return pr;
    }

    /**
     * If the lnurl-pay is disposable (true or null) we should not save the original lnurl.
     * If disposable is false, we can use it for example as a contact. Non disposable signals that
     * the lnurl-pay is not going to change in the future.
     */
    public boolean isDisposable() {
        return disposable;
    }

    /**
     * Currently supported tags for successAction object are url, message, and aes.
     * If there is no action then successAction is null.
     */
    public LnUrlPaySuccessAction getSuccessAction() {
        return successAction;
    }
}
