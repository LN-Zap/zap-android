package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Single;

public interface LndWalletUnlockerService {

    Single<com.github.lightningnetwork.lnd.lnrpc.GenSeedResponse> genSeed(com.github.lightningnetwork.lnd.lnrpc.GenSeedRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.InitWalletResponse> initWallet(com.github.lightningnetwork.lnd.lnrpc.InitWalletRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.UnlockWalletResponse> unlockWallet(com.github.lightningnetwork.lnd.lnrpc.UnlockWalletRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChangePasswordResponse> changePassword(com.github.lightningnetwork.lnd.lnrpc.ChangePasswordRequest request);
}