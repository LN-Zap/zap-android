package zapsolutions.zap.lnd;

import com.github.lightningnetwork.lnd.routerrpc.RouterGrpc;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class RemoteLndRouterService implements LndRouterService {

    private RouterGrpc.RouterStub asyncStub;

    public RemoteLndRouterService(Channel channel, CallCredentials callCredentials) {
        asyncStub = RouterGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.lnrpc.Payment> sendPaymentV2(com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.sendPaymentV2(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.lnrpc.Payment> trackPaymentV2(com.github.lightningnetwork.lnd.routerrpc.TrackPaymentRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.trackPaymentV2(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.RouteFeeResponse> estimateRouteFee(com.github.lightningnetwork.lnd.routerrpc.RouteFeeRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.estimateRouteFee(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.SendToRouteResponse> sendToRoute(com.github.lightningnetwork.lnd.routerrpc.SendToRouteRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.sendToRoute(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.lnrpc.HTLCAttempt> sendToRouteV2(com.github.lightningnetwork.lnd.routerrpc.SendToRouteRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.sendToRouteV2(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.ResetMissionControlResponse> resetMissionControl(com.github.lightningnetwork.lnd.routerrpc.ResetMissionControlRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.resetMissionControl(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.QueryMissionControlResponse> queryMissionControl(com.github.lightningnetwork.lnd.routerrpc.QueryMissionControlRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.queryMissionControl(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.QueryProbabilityResponse> queryProbability(com.github.lightningnetwork.lnd.routerrpc.QueryProbabilityRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.queryProbability(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Single<com.github.lightningnetwork.lnd.routerrpc.BuildRouteResponse> buildRoute(com.github.lightningnetwork.lnd.routerrpc.BuildRouteRequest request) {
        return DefaultSingle.createDefault(emitter -> asyncStub.buildRoute(request, new RemoteLndSingleObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.routerrpc.HtlcEvent> subscribeHtlcEvents(com.github.lightningnetwork.lnd.routerrpc.SubscribeHtlcEventsRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.subscribeHtlcEvents(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.routerrpc.PaymentStatus> sendPayment(com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.sendPayment(request, new RemoteLndStreamObserver<>(emitter)));
    }

    @Override
    public Observable<com.github.lightningnetwork.lnd.routerrpc.PaymentStatus> trackPayment(com.github.lightningnetwork.lnd.routerrpc.TrackPaymentRequest request) {
        return DefaultObservable.createDefault(emitter -> asyncStub.trackPayment(request, new RemoteLndStreamObserver<>(emitter)));
    }

}