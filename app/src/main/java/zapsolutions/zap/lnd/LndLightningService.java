package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface LndLightningService {

    Single<com.github.lightningnetwork.lnd.lnrpc.WalletBalanceResponse> walletBalance(com.github.lightningnetwork.lnd.lnrpc.WalletBalanceRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceResponse> channelBalance(com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.TransactionDetails> getTransactions(com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.EstimateFeeResponse> estimateFee(com.github.lightningnetwork.lnd.lnrpc.EstimateFeeRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.SendCoinsResponse> sendCoins(com.github.lightningnetwork.lnd.lnrpc.SendCoinsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListUnspentResponse> listUnspent(com.github.lightningnetwork.lnd.lnrpc.ListUnspentRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.Transaction> subscribeTransactions(com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.SendManyResponse> sendMany(com.github.lightningnetwork.lnd.lnrpc.SendManyRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.NewAddressResponse> newAddress(com.github.lightningnetwork.lnd.lnrpc.NewAddressRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.SignMessageResponse> signMessage(com.github.lightningnetwork.lnd.lnrpc.SignMessageRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.VerifyMessageResponse> verifyMessage(com.github.lightningnetwork.lnd.lnrpc.VerifyMessageRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ConnectPeerResponse> connectPeer(com.github.lightningnetwork.lnd.lnrpc.ConnectPeerRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.DisconnectPeerResponse> disconnectPeer(com.github.lightningnetwork.lnd.lnrpc.DisconnectPeerRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListPeersResponse> listPeers(com.github.lightningnetwork.lnd.lnrpc.ListPeersRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.PeerEvent> subscribePeerEvents(com.github.lightningnetwork.lnd.lnrpc.PeerEventSubscription request);

    Single<com.github.lightningnetwork.lnd.lnrpc.GetInfoResponse> getInfo(com.github.lightningnetwork.lnd.lnrpc.GetInfoRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.GetRecoveryInfoResponse> getRecoveryInfo(com.github.lightningnetwork.lnd.lnrpc.GetRecoveryInfoRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse> pendingChannels(com.github.lightningnetwork.lnd.lnrpc.PendingChannelsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListChannelsResponse> listChannels(com.github.lightningnetwork.lnd.lnrpc.ListChannelsRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.ChannelEventUpdate> subscribeChannelEvents(com.github.lightningnetwork.lnd.lnrpc.ChannelEventSubscription request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsResponse> closedChannels(com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChannelPoint> openChannelSync(com.github.lightningnetwork.lnd.lnrpc.OpenChannelRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.OpenStatusUpdate> openChannel(com.github.lightningnetwork.lnd.lnrpc.OpenChannelRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.BatchOpenChannelResponse> batchOpenChannel(com.github.lightningnetwork.lnd.lnrpc.BatchOpenChannelRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.FundingStateStepResp> fundingStateStep(com.github.lightningnetwork.lnd.lnrpc.FundingTransitionMsg request);

    // skipped ChannelAcceptor

    Observable<com.github.lightningnetwork.lnd.lnrpc.CloseStatusUpdate> closeChannel(com.github.lightningnetwork.lnd.lnrpc.CloseChannelRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.AbandonChannelResponse> abandonChannel(com.github.lightningnetwork.lnd.lnrpc.AbandonChannelRequest request);

    // skipped SendPayment

    Single<com.github.lightningnetwork.lnd.lnrpc.SendResponse> sendPaymentSync(com.github.lightningnetwork.lnd.lnrpc.SendRequest request);

    // skipped SendToRoute

    Single<com.github.lightningnetwork.lnd.lnrpc.SendResponse> sendToRouteSync(com.github.lightningnetwork.lnd.lnrpc.SendToRouteRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.AddInvoiceResponse> addInvoice(com.github.lightningnetwork.lnd.lnrpc.Invoice request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListInvoiceResponse> listInvoices(com.github.lightningnetwork.lnd.lnrpc.ListInvoiceRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.Invoice> lookupInvoice(com.github.lightningnetwork.lnd.lnrpc.PaymentHash request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.Invoice> subscribeInvoices(com.github.lightningnetwork.lnd.lnrpc.InvoiceSubscription request);

    Single<com.github.lightningnetwork.lnd.lnrpc.PayReq> decodePayReq(com.github.lightningnetwork.lnd.lnrpc.PayReqString request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListPaymentsResponse> listPayments(com.github.lightningnetwork.lnd.lnrpc.ListPaymentsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.DeletePaymentResponse> deletePayment(com.github.lightningnetwork.lnd.lnrpc.DeletePaymentRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.DeleteAllPaymentsResponse> deleteAllPayments(com.github.lightningnetwork.lnd.lnrpc.DeleteAllPaymentsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChannelGraph> describeGraph(com.github.lightningnetwork.lnd.lnrpc.ChannelGraphRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.NodeMetricsResponse> getNodeMetrics(com.github.lightningnetwork.lnd.lnrpc.NodeMetricsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChannelEdge> getChanInfo(com.github.lightningnetwork.lnd.lnrpc.ChanInfoRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.NodeInfo> getNodeInfo(com.github.lightningnetwork.lnd.lnrpc.NodeInfoRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.QueryRoutesResponse> queryRoutes(com.github.lightningnetwork.lnd.lnrpc.QueryRoutesRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.NetworkInfo> getNetworkInfo(com.github.lightningnetwork.lnd.lnrpc.NetworkInfoRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.StopResponse> stopDaemon(com.github.lightningnetwork.lnd.lnrpc.StopRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.GraphTopologyUpdate> subscribeChannelGraph(com.github.lightningnetwork.lnd.lnrpc.GraphTopologySubscription request);

    Single<com.github.lightningnetwork.lnd.lnrpc.DebugLevelResponse> debugLevel(com.github.lightningnetwork.lnd.lnrpc.DebugLevelRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.FeeReportResponse> feeReport(com.github.lightningnetwork.lnd.lnrpc.FeeReportRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.PolicyUpdateResponse> updateChannelPolicy(com.github.lightningnetwork.lnd.lnrpc.PolicyUpdateRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ForwardingHistoryResponse> forwardingHistory(com.github.lightningnetwork.lnd.lnrpc.ForwardingHistoryRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChannelBackup> exportChannelBackup(com.github.lightningnetwork.lnd.lnrpc.ExportChannelBackupRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ChanBackupSnapshot> exportAllChannelBackups(com.github.lightningnetwork.lnd.lnrpc.ChanBackupExportRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.VerifyChanBackupResponse> verifyChanBackup(com.github.lightningnetwork.lnd.lnrpc.ChanBackupSnapshot request);

    Single<com.github.lightningnetwork.lnd.lnrpc.RestoreBackupResponse> restoreChannelBackups(com.github.lightningnetwork.lnd.lnrpc.RestoreChanBackupRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.ChanBackupSnapshot> subscribeChannelBackups(com.github.lightningnetwork.lnd.lnrpc.ChannelBackupSubscription request);

    Single<com.github.lightningnetwork.lnd.lnrpc.BakeMacaroonResponse> bakeMacaroon(com.github.lightningnetwork.lnd.lnrpc.BakeMacaroonRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListMacaroonIDsResponse> listMacaroonIDs(com.github.lightningnetwork.lnd.lnrpc.ListMacaroonIDsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.DeleteMacaroonIDResponse> deleteMacaroonID(com.github.lightningnetwork.lnd.lnrpc.DeleteMacaroonIDRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.ListPermissionsResponse> listPermissions(com.github.lightningnetwork.lnd.lnrpc.ListPermissionsRequest request);

    Single<com.github.lightningnetwork.lnd.lnrpc.CheckMacPermResponse> checkMacaroonPermissions(com.github.lightningnetwork.lnd.lnrpc.CheckMacPermRequest request);

    // skipped RegisterRPCMiddleware

    Single<com.github.lightningnetwork.lnd.lnrpc.SendCustomMessageResponse> sendCustomMessage(com.github.lightningnetwork.lnd.lnrpc.SendCustomMessageRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.CustomMessage> subscribeCustomMessages(com.github.lightningnetwork.lnd.lnrpc.SubscribeCustomMessagesRequest request);
}