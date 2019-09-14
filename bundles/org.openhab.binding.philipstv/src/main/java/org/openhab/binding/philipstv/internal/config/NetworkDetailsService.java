/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.openhab.binding.philipstv.internal.service.*;
import org.openhab.binding.philipstv.internal.service.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.BASE_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.SLASH;

/**
 * Service class which only purpose is to deliver network details of the PhilipsTV.
 * @author Benjamin Meyer
 */
public class NetworkDetailsService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public String getMacFromEnabledInterface(CredentialDetails credentials,
      HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String path = BASE_PATH + "network" + SLASH + "devices";
    String jsonContent = new ConnectionService().doHttpsGet(credentials, target, path);
    JsonArray jsonArray = new JsonParser().parse(jsonContent).getAsJsonArray();
    for (JsonElement e : jsonArray) {
      JsonObject jsonObject = e.getAsJsonObject();
      if ("Enabled".equalsIgnoreCase(jsonObject.get("wake-on-lan").getAsString())) {
        return jsonObject.get("mac").getAsString();
      }
    }
    return "";
  }

}
