package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndWatchtowerClientService {

    Single<com.github.lightningnetwork.lnd.wtclientrpc.AddTowerResponse> addTower(com.github.lightningnetwork.lnd.wtclientrpc.AddTowerRequest request);

    Single<com.github.lightningnetwork.lnd.wtclientrpc.RemoveTowerResponse> removeTower(com.github.lightningnetwork.lnd.wtclientrpc.RemoveTowerRequest request);

    Single<com.github.lightningnetwork.lnd.wtclientrpc.ListTowersResponse> listTowers(com.github.lightningnetwork.lnd.wtclientrpc.ListTowersRequest request);

    Single<com.github.lightningnetwork.lnd.wtclientrpc.Tower> getTowerInfo(com.github.lightningnetwork.lnd.wtclientrpc.GetTowerInfoRequest request);

    Single<com.github.lightningnetwork.lnd.wtclientrpc.StatsResponse> stats(com.github.lightningnetwork.lnd.wtclientrpc.StatsRequest request);

    Single<com.github.lightningnetwork.lnd.wtclientrpc.PolicyResponse> policy(com.github.lightningnetwork.lnd.wtclientrpc.PolicyRequest request);
}