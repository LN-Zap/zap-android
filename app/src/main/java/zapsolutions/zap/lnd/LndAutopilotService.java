package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndAutopilotService {

    Single<com.github.lightningnetwork.lnd.autopilotrpc.StatusResponse> status(com.github.lightningnetwork.lnd.autopilotrpc.StatusRequest request);

    Single<com.github.lightningnetwork.lnd.autopilotrpc.ModifyStatusResponse> modifyStatus(com.github.lightningnetwork.lnd.autopilotrpc.ModifyStatusRequest request);

    Single<com.github.lightningnetwork.lnd.autopilotrpc.QueryScoresResponse> queryScores(com.github.lightningnetwork.lnd.autopilotrpc.QueryScoresRequest request);

    Single<com.github.lightningnetwork.lnd.autopilotrpc.SetScoresResponse> setScores(com.github.lightningnetwork.lnd.autopilotrpc.SetScoresRequest request);
}