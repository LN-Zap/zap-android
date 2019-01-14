package ln_zap.zap.connection;


import com.google.common.io.BaseEncoding;

import javax.net.ssl.SSLSocketFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

/**
 * Singleton to handle the connection to lnd
 *
 * Please note:
 * IP, Certificate and Macaroon are placeholders right now.
 */
public class LndConnection {
    private static LndConnection mLndConnectionInstance;
    private SSLSocketFactory mSSLFactory;
    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;


    private LndConnection() {
        // Macaroon
        String macaroonBase64UrlString = "AgEDbG5kAs8BAwoQ7a5y27C7Q4_FQFYaHP2uuBIBMBoWCgdhZGRyZXNzEgRyZWFkEgV3cml0ZRoTCgRpbmZvEgRyZWFkEgV3cml0ZRoXCghpbnZvaWNlcxIEcmVhZBIFd3JpdGUaFgoHbWVzc2FnZRIEcmVhZBIFd3JpdGUaFwoIb2ZmY2hhaW4SBHJlYWQSBXdyaXRlGhYKB29uY2hhaW4SBHJlYWQSBXdyaXRlGhQKBXBlZXJzEgRyZWFkEgV3cml0ZRoSCgZzaWduZXISCGdlbmVyYXRlAAAGIKUDrvb9TjXUpc3Dca_8zSZ6wcI4PWg7mqaPxh_oZZAX";
        byte[] macaroonBytes = BaseEncoding.base64Url().decode(macaroonBase64UrlString);
        String macaroon = BaseEncoding.base16().encode(macaroonBytes);
        mMacaroon = new MacaroonCallCredential(macaroon);


        // SSL
        String certificateBase64UrlString = "MIIB6zCCAZGgAwIBAgIRALk4MnZPN5DY9Zblrfdg26swCgYIKoZIzj0EAwIwMTEfMB0GA1UEChMWbG5kIGF1dG9nZW5lcmF0ZWQgY2VydDEOMAwGA1UEAxMFWmFwMDEwHhcNMTkwMTAzMTEzNjUxWhcNMjAwMjI4MTEzNjUxWjAxMR8wHQYDVQQKExZsbmQgYXV0b2dlbmVyYXRlZCBjZXJ0MQ4wDAYDVQQDEwVaYXAwMTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABIM_5RHSfEsj3bDD56goP8Fm4u8Kdh7DPyRZ8-xVEAz7YwXLm2zLJLYxCSBeMPbRKs7F5CuRePKrgBFr6tDdPoGjgYkwgYYwDgYDVR0PAQH_BAQDAgKkMA8GA1UdEwEB_wQFMAMBAf8wYwYDVR0RBFwwWoIFWmFwMDGCCWxvY2FsaG9zdIIEdW5peIIKdW5peHBhY2tldIcEfwAAAYcQAAAAAAAAAAAAAAAAAAAAAYcEneZhQocEChMABYcQ_oAAAAAAAABsUEr__iJjMjAKBggqhkjOPQQDAgNIADBFAiBWsPEhXswlcj2aVd05v6wjf5jBe_OCyjZEu5PRbMSzuQIhAJpZXCg62zy6jt6S0LenZ7o-X3yiByRpoeFbVjfo5jQo";
        byte[] certificateBytes = BaseEncoding.base64Url().decode(certificateBase64UrlString);

        mSSLFactory = CustomSSLSocketFactory.create(certificateBytes);

        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        mSecureChannel = OkHttpChannelBuilder
                .forAddress("157.230.97.66",10009)
                .sslSocketFactory(mSSLFactory)
                .build();
        // Maybe the channel has to be closed later...
        // secureChannel.shutdown();
    }


    public static synchronized LndConnection getInstance() {
        if (mLndConnectionInstance == null) {
            mLndConnectionInstance = new LndConnection();
        }
        return mLndConnectionInstance;
    }

    public SSLSocketFactory getSSLFactory() {
        return mSSLFactory;
    }

    public MacaroonCallCredential getMacaroon() {
        return mMacaroon;
    }

    public ManagedChannel getSecureChannel() {
        return mSecureChannel;
    }
}
