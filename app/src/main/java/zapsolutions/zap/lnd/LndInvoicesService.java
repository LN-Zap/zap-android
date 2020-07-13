package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface LndInvoicesService {

    Observable<com.github.lightningnetwork.lnd.lnrpc.Invoice> subscribeSingleInvoice(com.github.lightningnetwork.lnd.invoicesrpc.SubscribeSingleInvoiceRequest request);

    Single<com.github.lightningnetwork.lnd.invoicesrpc.CancelInvoiceResp> cancelInvoice(com.github.lightningnetwork.lnd.invoicesrpc.CancelInvoiceMsg request);

    Single<com.github.lightningnetwork.lnd.invoicesrpc.AddHoldInvoiceResp> addHoldInvoice(com.github.lightningnetwork.lnd.invoicesrpc.AddHoldInvoiceRequest request);

    Single<com.github.lightningnetwork.lnd.invoicesrpc.SettleInvoiceResp> settleInvoice(com.github.lightningnetwork.lnd.invoicesrpc.SettleInvoiceMsg request);
}