package zapsolutions.zap.connection.lndConnection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.grpc.HttpConnectProxiedSocketAddress;

public class ZapTorProxyDetector implements io.grpc.ProxyDetector {
    final InetSocketAddress mProxyAddress;

    public ZapTorProxyDetector(int proxyPort) {
        mProxyAddress = new InetSocketAddress("127.0.0.1", proxyPort);
    }

    @Override
    public HttpConnectProxiedSocketAddress proxyFor(SocketAddress targetAddress) {
        return HttpConnectProxiedSocketAddress.newBuilder()
                .setTargetAddress((InetSocketAddress) targetAddress)
                .setProxyAddress(mProxyAddress)
                .build();
    }
}
