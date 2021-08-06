package zapsolutions.zap.tor;

import java.util.Arrays;
import java.util.List;

import io.matthewnelson.topl_service_base.ApplicationDefaultTorSettings;

public class ZapTorSettings extends ApplicationDefaultTorSettings {
    @Override
    public String getConnectionPadding() {
        return ConnectionPadding.OFF;
    }

    @Override
    public String getCustomTorrc() {
        return null;
    }

    @Override
    public boolean getDisableNetwork() {
        return DEFAULT__DISABLE_NETWORK;
    }

    @Override
    public String getDnsPort() {
        return PortOption.DISABLED;
    }

    @Override
    public List<String> getDnsPortIsolationFlags() {
        return Arrays.asList(
                IsolationFlag.ISOLATE_CLIENT_PROTOCOL
        );
    }

    @Override
    public Integer getDormantClientTimeout() {
        return DEFAULT__DORMANT_CLIENT_TIMEOUT;
    }

    @Override
    public String getEntryNodes() {
        return DEFAULT__ENTRY_NODES;
    }

    @Override
    public String getExcludeNodes() {
        return DEFAULT__EXCLUDED_NODES;
    }

    @Override
    public String getExitNodes() {
        return DEFAULT__EXIT_NODES;
    }

    @Override
    public boolean getHasBridges() {
        return DEFAULT__HAS_BRIDGES;
    }

    @Override
    public boolean getHasCookieAuthentication() {
        return DEFAULT__HAS_COOKIE_AUTHENTICATION;
    }

    @Override
    public boolean getHasDebugLogs() {
        return DEFAULT__HAS_DEBUG_LOGS;
    }

    @Override
    public boolean getHasDormantCanceledByStartup() {
        return DEFAULT__HAS_DORMANT_CANCELED_BY_STARTUP;
    }

    @Override
    public boolean getHasOpenProxyOnAllInterfaces() {
        return DEFAULT__HAS_OPEN_PROXY_ON_ALL_INTERFACES;
    }

    @Override
    public boolean getHasReachableAddress() {
        return DEFAULT__HAS_REACHABLE_ADDRESS;
    }

    @Override
    public boolean getHasReducedConnectionPadding() {
        return DEFAULT__HAS_REDUCED_CONNECTION_PADDING;
    }

    @Override
    public boolean getHasSafeSocks() {
        return DEFAULT__HAS_SAFE_SOCKS;
    }

    @Override
    public boolean getHasStrictNodes() {
        return DEFAULT__HAS_STRICT_NODES;
    }

    @Override
    public boolean getHasTestSocks() {
        return DEFAULT__HAS_TEST_SOCKS;
    }

    @Override
    public String getHttpTunnelPort() {
        return "33591";
        //return PortOption.AUTO;
    }

    @Override
    public List<String> getHttpTunnelPortIsolationFlags() {
        return Arrays.asList(
                IsolationFlag.ISOLATE_CLIENT_PROTOCOL
        );
    }

    @Override
    public boolean isAutoMapHostsOnResolve() {
        return DEFAULT__IS_AUTO_MAP_HOSTS_ON_RESOLVE;
    }

    @Override
    public boolean isRelay() {
        return DEFAULT__IS_RELAY;
    }

    @Override
    public List<String> getListOfSupportedBridges() {
        return Arrays.asList(
                SupportedBridgeType.MEEK,
                SupportedBridgeType.OBFS4
        );
    }

    @Override
    public String getProxyHost() {
        return DEFAULT__PROXY_HOST;
    }

    @Override
    public String getProxyPassword() {
        return DEFAULT__PROXY_PASSWORD;
    }

    @Override
    public Integer getProxyPort() {
        return null;
    }

    @Override
    public String getProxySocks5Host() {
        return DEFAULT__PROXY_SOCKS5_HOST;
    }

    @Override
    public Integer getProxySocks5ServerPort() {
        return null;
    }

    @Override
    public String getProxyType() {
        return ProxyType.DISABLED;
    }

    @Override
    public String getProxyUser() {
        return DEFAULT__PROXY_USER;
    }

    @Override
    public String getReachableAddressPorts() {
        return DEFAULT__REACHABLE_ADDRESS_PORTS;
    }

    @Override
    public String getRelayNickname() {
        return DEFAULT__RELAY_NICKNAME;
    }

    @Override
    public String getRelayPort() {
        return PortOption.DISABLED;
    }

    @Override
    public boolean getRunAsDaemon() {
        return DEFAULT__RUN_AS_DAEMON;
    }

    @Override
    public String getSocksPort() {
        return PortOption.DISABLED;
    }

    @Override
    public List<String> getSocksPortIsolationFlags() {
        return Arrays.asList(
                IsolationFlag.KEEP_ALIVE_ISOLATE_SOCKS_AUTH,
                IsolationFlag.IPV6_TRAFFIC,
                IsolationFlag.PREFER_IPV6,
                IsolationFlag.ISOLATE_CLIENT_PROTOCOL
        );
    }

    @Override
    public String getTransPort() {
        return PortOption.DISABLED;
    }

    @Override
    public List<String> getTransPortIsolationFlags() {
        return Arrays.asList(
                IsolationFlag.ISOLATE_CLIENT_PROTOCOL
        );
    }

    @Override
    public boolean getUseSocks5() {
        return DEFAULT__USE_SOCKS5;
    }

    @Override
    public String getVirtualAddressNetwork() {
        return "10.192.0.2/10";
    }
}
