/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CONNECT_TIMEOUT_MILLISECONDS;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.HTTPS;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.MAX_REQUEST_RETRIES;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.SOCKET_TIMEOUT_MILLISECONDS;

import java.net.NoRouteToHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * The {@link ConnectionManagerUtil} is offering methods for connection specific processes.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public final class ConnectionManagerUtil {

    private ConnectionManagerUtil() {
    }

    public static CloseableHttpClient createSharedHttpClient(HttpHost target, String username, String password)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(username, password));

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT_MILLISECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLISECONDS).build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(getSslConnectionWithoutCertValidation(),
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register(HTTPS, sslsf).build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        HttpRequestRetryHandler requestRetryHandler = (exception, executionCount, context) -> {
            if (exception instanceof NoRouteToHostException) {
                return false;
            }
            if ((exception instanceof HttpHostConnectException)
                    && exception.getMessage().contains("Connection refused")) {
                return false;
            }
            return executionCount < MAX_REQUEST_RETRIES;
        };

        return HttpClients.custom().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf)
                .setDefaultCredentialsProvider(credProvider).setConnectionManager(connManager)
                .setRetryHandler(requestRetryHandler).setConnectionManagerShared(true).build();
    }

    private static SSLContext getSslConnectionWithoutCertValidation()
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        return new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build();
    }
}
