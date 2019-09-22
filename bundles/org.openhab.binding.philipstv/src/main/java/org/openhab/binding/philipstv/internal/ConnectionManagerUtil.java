/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CONNECT_TIMEOUT;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.HTTPS;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.SOCKET_TIMEOUT;

/**
 * The {@link ConnectionManagerUtil} is offering methods for connection specific processes.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public final class ConnectionManagerUtil {

    private ConnectionManagerUtil() {
    }

    public static CloseableHttpClient createSharedHttpClient(HttpHost target, String username, String password)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException,
            CertificateException {
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(username, password));

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(
                SOCKET_TIMEOUT).build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createSSLContextWithPhilipsTvCertificate(),
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTPS, sslsf).build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        return HttpClients.custom().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf)
                .setDefaultCredentialsProvider(credProvider).setConnectionManager(connManager)
                .setConnectionManagerShared(true).build();
    }

    private static SSLContext createSSLContextWithPhilipsTvCertificate()
            throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException,
            KeyStoreException {

        SSLContext sslContext;
        try (InputStream is = Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource("xtv_ca.crt")).openStream()) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null); // You don't need the KeyStore instance to come from a file.
            ks.setCertificateEntry("caCert", caCert);

            tmf.init(ks);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        }
        return sslContext;
    }
}
