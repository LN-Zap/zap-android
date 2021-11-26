package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.wtclientrpc.WatchtowerClientGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndWatchtowerClientService implements LndWatchtowerClientService {

    private final WatchtowerClientGrpc.WatchtowerClientStub asyncStub;

    public RemoteLndWatchtowerClientService(Channel channel, CallCredentials callCredentials) {
        asyncStub = WatchtowerClientGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.AddTowerResponse> addTower(com.github.lightningnetwork.lnd.wtclientrpc.AddTowerRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.addTower(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.RemoveTowerResponse> removeTower(com.github.lightningnetwork.lnd.wtclientrpc.RemoveTowerRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.removeTower(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.ListTowersResponse> listTowers(com.github.lightningnetwork.lnd.wtclientrpc.ListTowersRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.listTowers(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.Tower> getTowerInfo(com.github.lightningnetwork.lnd.wtclientrpc.GetTowerInfoRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.getTowerInfo(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.StatsResponse> stats(com.github.lightningnetwork.lnd.wtclientrpc.StatsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.stats(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.wtclientrpc.PolicyResponse> policy(com.github.lightningnetwork.lnd.wtclientrpc.PolicyRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.policy(request, new RemoteLndSingleObserver<>(emitter)));
    }

}