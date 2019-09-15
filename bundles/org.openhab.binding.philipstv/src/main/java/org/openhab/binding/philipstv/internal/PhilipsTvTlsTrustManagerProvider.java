/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.auth.x500.X500Principal;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Provides a TrustManager to allow secure https connections to any Philips TV
 * TODO: convert to using TrustAllTrustMananger (not supported in OH2.4 yet)
 *
 */
@Component
@NonNullByDefault
public class PhilipsTvTlsTrustManagerProvider implements TlsTrustManagerProvider {
    private final Logger logger = LoggerFactory.getLogger(PhilipsTvTlsTrustManagerProvider.class);

    @Override
    public String getHostName() {
        return "restfultv.tpvision.com";
    }

    private final X509ExtendedTrustManager trustAllCerts = new X509ExtendedTrustManager() {

        private void logCertificateCN(String function, X509Certificate @Nullable [] x509Certificates) {
            if (x509Certificates != null && logger.isTraceEnabled()) {
                for (X509Certificate certificate : x509Certificates) {
                    logger.trace("TrustManager {} CN: {}", function,
                            certificate.getSubjectX500Principal().getName(X500Principal.RFC2253));
                }
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s)
                throws CertificateException {
            logCertificateCN("checkClientTrusted", x509Certificates);
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s)
                throws CertificateException {
            logCertificateCN("checkServerTrusted", x509Certificates);
        }

        @Override
        public X509Certificate @Nullable [] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable Socket socket) throws CertificateException {
            logCertificateCN("checkClientTrustedSocket", x509Certificates);
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable Socket socket) throws CertificateException {
            logCertificateCN("checkServerTrustedSocket", x509Certificates);
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable SSLEngine sslEngine) throws CertificateException {
            logCertificateCN("checkClientTrustedEngine", x509Certificates);
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable SSLEngine sslEngine) throws CertificateException {
            logCertificateCN("checkServerTrustedEngine", x509Certificates);
        }
    };

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return trustAllCerts;
        // return TrustAllTrustMananger.getInstance();
    }
}
