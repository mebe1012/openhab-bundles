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
import org.openhab.binding.philipstv.internal.service.model.CredentialDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * The {@link ConnectionService} is responsible for handling https GETs and POSTs to the Philips
 * TVs.
 * @author Benjamin Meyer - Initial contribution
 */
public class ConnectionService {

  public static final String TARGET_URI_MSG = "Target Uri is: {}";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public String doHttpsGet(CredentialDetails credentials, HttpHost target,
      String path) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String uri = target.toURI() + path;
    logger.debug(TARGET_URI_MSG, uri);
    HttpGet httpGet = new HttpGet(uri);
    String jsonContent = null;
    CloseableHttpResponse response = null;
    try (CloseableHttpClient client = ConnectionUtil.createClientWithCredentials(target, credentials)) {
      try {
        response = client.execute(target, httpGet);
      } catch (SocketTimeoutException ex) {
        for (int i = 0; i < 3; i++) {
          try {
            logger.debug("Read timed out exception occurred, trying GET again.");
            response = client.execute(target, httpGet);
            break;
          } catch (SocketTimeoutException e) {
          }
        }
      }
      if(response == null) {
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

  public String doHttpsPost(CredentialDetails credentials, HttpHost target, String
      path, String json) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    logger.debug(TARGET_URI_MSG, target.toURI() + path);
    HttpPost httpPost = new HttpPost(target.toURI() + path);
    httpPost.setHeader("Content-type", "application/json");
    httpPost.setEntity(new StringEntity(json));
    CloseableHttpResponse response = null;
    String jsonContent = "";
    try (CloseableHttpClient client = ConnectionUtil.createClientWithCredentials(target, credentials)) {
      try {
        response = client.execute(target, httpPost);
      } catch (SocketTimeoutException ex) {
        if ("Read timed out".equals(ex.getMessage())) {
          for (int i = 0; i < 3; i++) {
            try {
              logger.debug("Read timed out exception occurred, trying POST again.");
              response = client.execute(target, httpPost);
              break;
            } catch (SocketTimeoutException e) {
            }
          }
        }
      }
      if(response == null) {
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

  public byte[] doHttpsGetForImage(CredentialDetails credentials, HttpHost target, String
      path) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String uri = target.toURI() + path;
    logger.debug(TARGET_URI_MSG, uri);
    HttpGet httpGet = new HttpGet(uri);
    try (CloseableHttpClient client1 = ConnectionUtil.createClientWithCredentials(target, credentials);
         CloseableHttpResponse response = client1.execute(target, httpGet)) {
      if((response != null) && (response.getStatusLine().getStatusCode() == 401)) {
        throw new HttpResponseException(401, "The given username/password combination is invalid.");
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      response.getEntity().writeTo(baos);
      return baos.toByteArray();
    }
  }
}
