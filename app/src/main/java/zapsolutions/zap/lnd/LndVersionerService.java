package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndVersionerService {

    Single<com.github.lightningnetwork.lnd.verrpc.Version> getVersion(com.github.lightningnetwork.lnd.verrpc.VersionRequest request);
}