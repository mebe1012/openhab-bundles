/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.service;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.openhab.binding.philipstv.internal.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * The {@link ConnectionService} is responsible for handling https GETs and POSTs to the Philips
 * TVs.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class ConnectionService {

    public static final String TARGET_URI_MSG = "Target Uri is: {}";

    public static HttpHost HTTP_HOST;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String doHttpsGet(String path) throws IOException {
        String uri = HTTP_HOST.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpGet httpGet = new HttpGet(uri);
        String jsonContent;
        CloseableHttpResponse response = null;
        try (CloseableHttpClient client = ConnectionUtil.getSharedHttpClient()) {
            try {
                response = client.execute(HTTP_HOST, httpGet);
            } catch (SocketTimeoutException ex) {
                for (int i = 0; i < 3; i++) {
                    try {
                        logger.debug("Read timed out exception occurred, trying GET again.");
                        response = client.execute(HTTP_HOST, httpGet);
                        break;
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            }
            if (response == null) {
                throw new HttpResponseException(0, "The response for the GET request was empty.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                throw new HttpResponseException(401, "The given username/password combination is invalid.");
            }
            jsonContent = getJsonFromResponse(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return jsonContent;
    }

    public String doHttpsPost(String path, String json) throws IOException {
        String uri = HTTP_HOST.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(json));
        CloseableHttpResponse response = null;
        String jsonContent = "";
        try (CloseableHttpClient client = ConnectionUtil.getSharedHttpClient()) {
            try {
                response = client.execute(HTTP_HOST, httpPost);
            } catch (SocketTimeoutException ex) {
                if ("Read timed out".equals(ex.getMessage())) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            logger.debug("Read timed out exception occurred, trying POST again.");
                            response = client.execute(HTTP_HOST, httpPost);
                            break;
                        } catch (SocketTimeoutException ignored) {
                        }
                    }
                }
            }
            if (response == null) {
                throw new HttpResponseException(0, "The response for the POST request was empty.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                throw new HttpResponseException(401, "The given username/password combination is invalid.");
            }
            jsonContent = getJsonFromResponse(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return jsonContent;
    }

    private String getJsonFromResponse(HttpResponse response) throws IOException {
        String jsonContent = EntityUtils.toString(response.getEntity());
        logger.debug("----------------------------------------");
        logger.debug("{}", response.getStatusLine());
        logger.debug("{}", jsonContent);
        return jsonContent;
    }

    public byte[] doHttpsGetForImage(String path) throws IOException {
        String uri = HTTP_HOST.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpClient client = ConnectionUtil.getSharedHttpClient();
                CloseableHttpResponse response = client.execute(HTTP_HOST, httpGet)) {
            if ((response != null) && (response.getStatusLine().getStatusCode() == 401)) {
                throw new HttpResponseException(401, "The given username/password combination is invalid.");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (response != null) {
                response.getEntity().writeTo(baos);
            }
            return baos.toByteArray();
        }
    }
}
