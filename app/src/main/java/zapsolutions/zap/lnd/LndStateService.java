package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface LndStateService {

    Observable<com.github.lightningnetwork.lnd.lnrpc.SubscribeStateResponse> subscribeState(com.github.lightningnetwork.lnd.lnrpc.SubscribeStateRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.GetStateResponse> getState(com.github.lightningnetwork.lnd.lnrpc.GetStateRequest request);
}