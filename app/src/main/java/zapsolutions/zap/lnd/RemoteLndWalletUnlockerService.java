package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.lnrpc.WalletUnlockerGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndWalletUnlockerService implements LndWalletUnlockerService {

    private final WalletUnlockerGrpc.WalletUnlockerStub asyncStub;

    public RemoteLndWalletUnlockerService(Channel channel, CallCredentials callCredentials) {
        asyncStub = WalletUnlockerGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.GenSeedResponse> genSeed(com.github.lightningnetwork.lnd.lnrpc.GenSeedRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.genSeed(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.InitWalletResponse> initWallet(com.github.lightningnetwork.lnd.lnrpc.InitWalletRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.initWallet(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.UnlockWalletResponse> unlockWallet(com.github.lightningnetwork.lnd.lnrpc.UnlockWalletRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.unlockWallet(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.ChangePasswordResponse> changePassword(com.github.lightningnetwork.lnd.lnrpc.ChangePasswordRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.changePassword(request, new RemoteLndSingleObserver<>(emitter)));
    }

}