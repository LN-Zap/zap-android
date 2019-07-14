package zapsolutions.zap.connection;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Creates an SSLSocketFactory instance for use with a custom CA,
 * which would otherwise be considered "not trustworthy".
 * This can be fed into HttpsURLConnection, as well as networking libraries such as OkHttp's OkHttpClient.
 */
public final class CustomSSLSocketFactory {

    private CustomSSLSocketFactory() {
        throw new AssertionError();
    }

    /**
     * Creates an SSLSocketFactory instance for use with the provided CA certificate.
     *
     * @param certificate Certificate as byte array
     * @return An SSLSocketFactory which trusts the provided CA when provided to network clients
     */
    public static SSLSocketFactory create(byte[] certificate) {
        InputStream caInput = null;
        try {
            // Generate the CA Certificate from the supplied byte array
            caInput = new ByteArrayInputStream(certificate);
            Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);

            // Load the key store using the CA
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Initialize the TrustManager with this CA
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // Create an SSL context that uses the created trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();

        } catch (Exception ex) {
            throw new RuntimeException(ex);

        } finally {
            if (caInput != null) {
                try {
                    caInput.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
