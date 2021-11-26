package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.chainrpc.ChainNotifierGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Observable;

public class RemoteLndChainNotifierService implements LndChainNotifierService {

    private final ChainNotifierGrpc.ChainNotifierStub asyncStub;

    public RemoteLndChainNotifierService(Channel channel, CallCredentials callCredentials) {
        asyncStub = ChainNotifierGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.chainrpc.ConfEvent> registerConfirmationsNtfn(com.github.lightningnetwork.lnd.chainrpc.ConfRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.registerConfirmationsNtfn(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.chainrpc.SpendEvent> registerSpendNtfn(com.github.lightningnetwork.lnd.chainrpc.SpendRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.registerSpendNtfn(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.chainrpc.BlockEpoch> registerBlockEpochNtfn(com.github.lightningnetwork.lnd.chainrpc.BlockEpoch request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.registerBlockEpochNtfn(request, new RemoteLndStreamObserver<>(emitter)));
    }

}