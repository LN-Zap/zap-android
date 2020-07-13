package zapsolutions.zap.lnd;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface LndRouterService {

    Observable<com.github.lightningnetwork.lnd.lnrpc.Payment> sendPaymentV2(com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest request);

    Observable<com.github.lightningnetwork.lnd.lnrpc.Payment> trackPaymentV2(com.github.lightningnetwork.lnd.routerrpc.TrackPaymentRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.RouteFeeResponse> estimateRouteFee(com.github.lightningnetwork.lnd.routerrpc.RouteFeeRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.SendToRouteResponse> sendToRoute(com.github.lightningnetwork.lnd.routerrpc.SendToRouteRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.ResetMissionControlResponse> resetMissionControl(com.github.lightningnetwork.lnd.routerrpc.ResetMissionControlRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.QueryMissionControlResponse> queryMissionControl(com.github.lightningnetwork.lnd.routerrpc.QueryMissionControlRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.QueryProbabilityResponse> queryProbability(com.github.lightningnetwork.lnd.routerrpc.QueryProbabilityRequest request);

    Single<com.github.lightningnetwork.lnd.routerrpc.BuildRouteResponse> buildRoute(com.github.lightningnetwork.lnd.routerrpc.BuildRouteRequest request);

    Observable<com.github.lightningnetwork.lnd.routerrpc.HtlcEvent> subscribeHtlcEvents(com.github.lightningnetwork.lnd.routerrpc.SubscribeHtlcEventsRequest request);

    Observable<com.github.lightningnetwork.lnd.routerrpc.PaymentStatus> sendPayment(com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest request);

    Observable<com.github.lightningnetwork.lnd.routerrpc.PaymentStatus> trackPayment(com.github.lightningnetwork.lnd.routerrpc.TrackPaymentRequest request);
}