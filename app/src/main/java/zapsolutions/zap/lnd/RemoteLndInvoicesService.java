package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.invoicesrpc.InvoicesGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndInvoicesService implements LndInvoicesService {

    private final InvoicesGrpc.InvoicesStub asyncStub;

    public RemoteLndInvoicesService(Channel channel, CallCredentials callCredentials) {
        asyncStub = InvoicesGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.lnrpc.Invoice> subscribeSingleInvoice(com.github.lightningnetwork.lnd.invoicesrpc.SubscribeSingleInvoiceRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.subscribeSingleInvoice(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.invoicesrpc.CancelInvoiceResp> cancelInvoice(com.github.lightningnetwork.lnd.invoicesrpc.CancelInvoiceMsg request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.cancelInvoice(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.invoicesrpc.AddHoldInvoiceResp> addHoldInvoice(com.github.lightningnetwork.lnd.invoicesrpc.AddHoldInvoiceRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.addHoldInvoice(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.invoicesrpc.SettleInvoiceResp> settleInvoice(com.github.lightningnetwork.lnd.invoicesrpc.SettleInvoiceMsg request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.settleInvoice(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.Invoice> lookupInvoiceV2(com.github.lightningnetwork.lnd.invoicesrpc.LookupInvoiceMsg request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.lookupInvoiceV2(request, new RemoteLndSingleObserver<>(emitter)));
    }

}