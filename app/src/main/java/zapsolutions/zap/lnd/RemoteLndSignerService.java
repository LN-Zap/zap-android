package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.signrpc.SignerGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndSignerService implements LndSignerService {

    private SignerGrpc.SignerStub asyncStub;

    public RemoteLndSignerService(Channel channel, CallCredentials callCredentials) {
        asyncStub = SignerGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.SignResp> signOutputRaw(com.github.lightningnetwork.lnd.signrpc.SignReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.signOutputRaw(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.InputScriptResp> computeInputScript(com.github.lightningnetwork.lnd.signrpc.SignReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.computeInputScript(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.SignMessageResp> signMessage(com.github.lightningnetwork.lnd.signrpc.SignMessageReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.signMessage(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.VerifyMessageResp> verifyMessage(com.github.lightningnetwork.lnd.signrpc.VerifyMessageReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.verifyMessage(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.SharedKeyResponse> deriveSharedKey(com.github.lightningnetwork.lnd.signrpc.SharedKeyRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.deriveSharedKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

}