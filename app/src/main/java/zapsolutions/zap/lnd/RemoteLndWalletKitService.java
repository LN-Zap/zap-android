package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.walletrpc.WalletKitGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndWalletKitService implements LndWalletKitService {

    private final WalletKitGrpc.WalletKitStub asyncStub;

    public RemoteLndWalletKitService(Channel channel, CallCredentials callCredentials) {
        asyncStub = WalletKitGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ListUnspentResponse> listUnspent(com.github.lightningnetwork.lnd.walletrpc.ListUnspentRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.listUnspent(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.LeaseOutputResponse> leaseOutput(com.github.lightningnetwork.lnd.walletrpc.LeaseOutputRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.leaseOutput(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ReleaseOutputResponse> releaseOutput(com.github.lightningnetwork.lnd.walletrpc.ReleaseOutputRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.releaseOutput(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ListLeasesResponse> listLeases(com.github.lightningnetwork.lnd.walletrpc.ListLeasesRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.listLeases(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.KeyDescriptor> deriveNextKey(com.github.lightningnetwork.lnd.walletrpc.KeyReq request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.deriveNextKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.signrpc.KeyDescriptor> deriveKey(com.github.lightningnetwork.lnd.signrpc.KeyLocator request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.deriveKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.AddrResponse> nextAddr(com.github.lightningnetwork.lnd.walletrpc.AddrRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.nextAddr(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ListAccountsResponse> listAccounts(com.github.lightningnetwork.lnd.walletrpc.ListAccountsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.listAccounts(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ImportAccountResponse> importAccount(com.github.lightningnetwork.lnd.walletrpc.ImportAccountRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.importAccount(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ImportPublicKeyResponse> importPublicKey(com.github.lightningnetwork.lnd.walletrpc.ImportPublicKeyRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.importPublicKey(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.PublishResponse> publishTransaction(com.github.lightningnetwork.lnd.walletrpc.Transaction request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.publishTransaction(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.SendOutputsResponse> sendOutputs(com.github.lightningnetwork.lnd.walletrpc.SendOutputsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.sendOutputs(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.EstimateFeeResponse> estimateFee(com.github.lightningnetwork.lnd.walletrpc.EstimateFeeRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.estimateFee(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.PendingSweepsResponse> pendingSweeps(com.github.lightningnetwork.lnd.walletrpc.PendingSweepsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.pendingSweeps(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.BumpFeeResponse> bumpFee(com.github.lightningnetwork.lnd.walletrpc.BumpFeeRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.bumpFee(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.ListSweepsResponse> listSweeps(com.github.lightningnetwork.lnd.walletrpc.ListSweepsRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.listSweeps(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.LabelTransactionResponse> labelTransaction(com.github.lightningnetwork.lnd.walletrpc.LabelTransactionRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.labelTransaction(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.FundPsbtResponse> fundPsbt(com.github.lightningnetwork.lnd.walletrpc.FundPsbtRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.fundPsbt(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.walletrpc.FinalizePsbtResponse> finalizePsbt(com.github.lightningnetwork.lnd.walletrpc.FinalizePsbtRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.finalizePsbt(request, new RemoteLndSingleObserver<>(emitter)));
    }

}