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
package org.openhab.binding.neeo.internal.net;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.filter.LoggingFilter;
import org.openhab.binding.neeo.internal.NeeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP session with a client
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class HttpRequest implements AutoCloseable {

    /** the logger */
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The client to use */
    private final Client client;

    /**
     * Instantiates a new request
     */
    public HttpRequest(ClientBuilder clientBuilder) {
        client = clientBuilder.build();

        if (logger.isDebugEnabled()) {
            client.register(new LoggingFilter(new Slf4LoggingAdapter(logger), true));
        }
    }

    /**
     * Send a get command to the specified uri
     *
     * @param uri the non-null uri
     * @return the {@link HttpResponse}
     */
    public HttpResponse sendGetCommand(String uri) {
        NeeoUtil.requireNotEmpty(uri, "uri cannot be empty");
        try {
            final Builder request = client.target(uri).request();

            final Response content = request.get();

            try {
                return new HttpResponse(content);
            } finally {
                content.close();
            }
        } catch (IOException | IllegalStateException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        }
    }

    /**
     * Send post JSON command using the body
     *
     * @param uriString the non empty uri
     * @param body the non-null, possibly empty body
     * @return the {@link HttpResponse}
     */
    public HttpResponse sendPostJsonCommand(String uriString, String body) {
        NeeoUtil.requireNotEmpty(uriString, "uri cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");

        logger.trace("sendPostJsonCommand: target={}, body={}", uriString, body);

        try {
            URI targetUri = new URI(uriString);
            if (!targetUri.isAbsolute()) {
                logger.warn("Absolute URI required but provided URI '{}' is non-absolute. ", uriString);
                return new HttpResponse(HttpStatus.NOT_ACCEPTABLE_406, "Absolute URI required");
            }
            final Builder request = client.target(targetUri).request(MediaType.APPLICATION_JSON);

            final Response content = request.post(Entity.entity(body, MediaType.APPLICATION_JSON));

            try {
                return new HttpResponse(content);
            } finally {
                content.close();
            }
            // IllegalArgumentException/ProcessingException catches issues with the URI being invalid
            // as well
        } catch (IOException | IllegalStateException | IllegalArgumentException | ProcessingException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        } catch (URISyntaxException e) {
            return new HttpResponse(HttpStatus.NOT_ACCEPTABLE_406, e.getMessage());
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
