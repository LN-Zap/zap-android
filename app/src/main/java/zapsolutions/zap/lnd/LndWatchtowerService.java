package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndWatchtowerService {

    Single<com.github.lightningnetwork.lnd.watchtowerrpc.GetInfoResponse> getInfo(com.github.lightningnetwork.lnd.watchtowerrpc.GetInfoRequest request);
}