package zapsolutions.zap.connection.lndConnection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import io.grpc.HttpConnectProxiedSocketAddress;

public class ZapProxyDetector implements io.grpc.ProxyDetector {
    final InetSocketAddress mProxyAddress;

    public ZapProxyDetector(String proxyName, int proxyPort) throws UnknownHostException {
        mProxyAddress = new InetSocketAddress(InetAddress.getByName(proxyName), proxyPort);
    }

    @Override
    public HttpConnectProxiedSocketAddress proxyFor(SocketAddress targetAddress) {
        return HttpConnectProxiedSocketAddress.newBuilder()
                .setTargetAddress((InetSocketAddress) targetAddress)
                .setProxyAddress(mProxyAddress)
                .build();
    }
}
