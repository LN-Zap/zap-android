package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndSignerService {

    Single<com.github.lightningnetwork.lnd.signrpc.SignResp> signOutputRaw(com.github.lightningnetwork.lnd.signrpc.SignReq request);

    Single<com.github.lightningnetwork.lnd.signrpc.InputScriptResp> computeInputScript(com.github.lightningnetwork.lnd.signrpc.SignReq request);

    Single<com.github.lightningnetwork.lnd.signrpc.SignMessageResp> signMessage(com.github.lightningnetwork.lnd.signrpc.SignMessageReq request);

    Single<com.github.lightningnetwork.lnd.signrpc.VerifyMessageResp> verifyMessage(com.github.lightningnetwork.lnd.signrpc.VerifyMessageReq request);

    Single<com.github.lightningnetwork.lnd.signrpc.SharedKeyResponse> deriveSharedKey(com.github.lightningnetwork.lnd.signrpc.SharedKeyRequest request);
}