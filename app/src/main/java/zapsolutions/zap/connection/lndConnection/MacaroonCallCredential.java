package zapsolutions.zap.connection.lndConnection;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

/**
 * This class is used to create macaroon CallCredentials for the gRPC calls.
 */
public class MacaroonCallCredential extends CallCredentials {
    private final String macaroon;

    /**
     * The macaroon this MacaroonCallCredential is based on.
     *
     * @param macaroon Supply this as Hex-encoded string.
     */
    public MacaroonCallCredential(String macaroon) {
        this.macaroon = macaroon;
    }

    public void thisUsesUnstableApi() {
    }

    public void applyRequestMetadata(
            RequestInfo requestInfo,
            Executor executor,
            final MetadataApplier metadataApplier
    ) {
        String authority = requestInfo.getAuthority();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Metadata headers = new Metadata();
                    Metadata.Key<String> macaroonKey = Metadata.Key.of("macaroon", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(macaroonKey, macaroon);
                    metadataApplier.apply(headers);
                } catch (Throwable e) {
                    metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                }
            }
        });
    }
}