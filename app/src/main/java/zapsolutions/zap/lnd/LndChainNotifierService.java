package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Observable;

public interface LndChainNotifierService {

    Observable<com.github.lightningnetwork.lnd.chainrpc.ConfEvent> registerConfirmationsNtfn(com.github.lightningnetwork.lnd.chainrpc.ConfRequest request);

    Observable<com.github.lightningnetwork.lnd.chainrpc.SpendEvent> registerSpendNtfn(com.github.lightningnetwork.lnd.chainrpc.SpendRequest request);

    Observable<com.github.lightningnetwork.lnd.chainrpc.BlockEpoch> registerBlockEpochNtfn(com.github.lightningnetwork.lnd.chainrpc.BlockEpoch request);
}