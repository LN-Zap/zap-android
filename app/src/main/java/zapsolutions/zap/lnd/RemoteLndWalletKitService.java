package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.walletrpc.WalletKitGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndWalletKitService implements LndWalletKitService {

    private WalletKitGrpc.WalletKitStub asyncStub;

    public RemoteLndWalletKitService(Channel channel, CallCredentials callCredentials) {
        asyncStub = WalletKitGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.KeyDescriptor> deriveNextKey(com.github.lightningnetwork.lnd.walletrpc.KeyReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.deriveNextKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.KeyDescriptor> deriveKey(com.github.lightningnetwork.lnd.signrpc.KeyLocator request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.deriveKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.AddrResponse> nextAddr(com.github.lightningnetwork.lnd.walletrpc.AddrRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.nextAddr(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.PublishResponse> publishTransaction(com.github.lightningnetwork.lnd.walletrpc.Transaction request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.publishTransaction(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.SendOutputsResponse> sendOutputs(com.github.lightningnetwork.lnd.walletrpc.SendOutputsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.sendOutputs(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.EstimateFeeResponse> estimateFee(com.github.lightningnetwork.lnd.walletrpc.EstimateFeeRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.estimateFee(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.PendingSweepsResponse> pendingSweeps(com.github.lightningnetwork.lnd.walletrpc.PendingSweepsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.pendingSweeps(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.BumpFeeResponse> bumpFee(com.github.lightningnetwork.lnd.walletrpc.BumpFeeRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.bumpFee(request, new RemoteLndSingleObserver<>(emitter)));
    }

}