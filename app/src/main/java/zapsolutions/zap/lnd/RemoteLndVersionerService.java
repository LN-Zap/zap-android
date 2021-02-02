package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.verrpc.VersionerGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndVersionerService implements LndVersionerService {

    private VersionerGrpc.VersionerStub asyncStub;

    public RemoteLndVersionerService(Channel channel, CallCredentials callCredentials) {
        asyncStub = VersionerGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.verrpc.Version> getVersion(com.github.lightningnetwork.lnd.verrpc.VersionRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.getVersion(request, new RemoteLndSingleObserver<>(emitter)));
    }

}