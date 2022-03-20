package zapsolutions.zap.connection.lndConnection;

import com.google.common.io.BaseEncoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import zapsolutions.zap.connection.manageNodeConfigs.NodeConfig;
import zapsolutions.zap.util.ZapLog;

/**
 * Creates an SSLSocketFactory instance for use with a self signed Certificate,
 * which would otherwise be considered "not trustworthy".
 * This can be fed into HttpsURLConnection, as well as networking libraries such as OkHttp's OkHttpClient.
 */
public class LndSSLSocketFactory {

    private static final String LOG_TAG = LndSSLSocketFactory.class.getName();

    private LndSSLSocketFactory() {
        throw new AssertionError();
    }

    public static SSLSocketFactory create(NodeConfig nodeConfig) {
        SSLContext sslCtx = null;

        try {
            sslCtx = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        if (nodeConfig.isTor()) {
            // Always trust the certificate on Tor connection
            try {
                sslCtx.init(null, new TrustManager[]{new BlindTrustManager()}, null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
                return null;
            }
            return sslCtx.getSocketFactory();

        } else {
            // On clearnet we want to validate the certificate.
            if (nodeConfig.getCert() != null && !nodeConfig.getCert().isEmpty()) {
                //try to create a trustmanager that trust the certificate that was transmitted with the lndconnect string.
                try {
                    InputStream caInput = null;
                    String certificateBase64UrlString = nodeConfig.getCert();
                    byte[] certificateBytes = BaseEncoding.base64Url().decode(certificateBase64UrlString);

                    // Generate the CA Certificate from the supplied byte array
                    caInput = new ByteArrayInputStream(certificateBytes);
                    Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);

                    // Load the key store using the CA
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

                    // Initialize the TrustManager with this CA
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keyStore);

                    // Create an SSL context that uses the created trust manager
                    sslCtx.init(null, tmf.getTrustManagers(), new SecureRandom());
                    return sslCtx.getSocketFactory();

                } catch (Exception e) {
                    ZapLog.e(LOG_TAG, "Error while initializing self signed certificate.");
                    e.printStackTrace();
                }
            }
        }

        // If the above failed, use the default TrustManager which is used when set to null
        // This will be the case for btc pay for example as no self signed certificates are used
        try {
            sslCtx.init(null, null, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
        return sslCtx.getSocketFactory();
    }
}
