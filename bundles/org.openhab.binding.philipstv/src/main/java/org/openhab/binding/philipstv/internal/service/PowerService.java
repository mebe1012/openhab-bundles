/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.model.CredentialDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_OFF;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_POWERSTATE_PATH;
import static org.openhab.binding.philipstv.internal.service.KeyCode.KEY_STANDBY;

/**
 * The {@link PowerService} is responsible for handling power states commands, which are sent to the
 * power channel.
 * @author Benjamin Meyer - Initial contribution
 */
public class PowerService implements PhilipsTvService {

  public static final int DELAY_MILLIS = 2 * 1000;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConnectionService connectionService = new ConnectionService();

  @Override
  public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
    try {
      if (command instanceof RefreshType) {
        String powerState = getPowerState(handler.credentials, handler.target);
        if (powerState.equals(POWER_ON)) {
          handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
        } else if (powerState.equals(POWER_OFF)) {
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
        }
      } else if (command instanceof OnOffType) {
        setPowerState(handler.credentials, command, handler.target);
        if (command == OnOffType.ON) {
          handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Tv turned on.");
        } else {
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Tv turned off.");
        }
      } else {
        logger.warn("Unknown command: {} for Channel {}", command, channel);
      }
    } catch (Exception e) {
      if (isTvOfflineException(e)) {
        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
      } else if (isTvNotListeningException(e)) {
        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, TV_NOT_LISTENING_MSG);
      } else {
        logger.warn("Unexpected Error handling the PowerState command {} for Channel {}: {}", command, channel, e.getMessage(), e);
      }
    }
  }

  private String getPowerState(CredentialDetails credentials,
      HttpHost target) throws IOException, ParseException, JsonSyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String jsonContent = connectionService.doHttpsGet(credentials, target, TV_POWERSTATE_PATH);
    JsonObject jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();
    String powerState = jsonObject.get("powerstate").getAsString();
    return powerState.equalsIgnoreCase(POWER_ON) ? POWER_ON : POWER_OFF;
  }

  private void setPowerState(CredentialDetails credentials, Command command,
      HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    JsonObject powerStateJson = new JsonObject();
    if (command.equals(OnOffType.ON)) {
      powerStateJson.addProperty("powerstate", POWER_ON);
    } else { // OFF
      powerStateJson.addProperty("powerstate", KEY_STANDBY.toString());
    }
    connectionService.doHttpsPost(credentials, target, TV_POWERSTATE_PATH, powerStateJson.toString());
  }

//  private void setPowerState(CredentialDetails credentials, Command command, HttpHost target,
//      String mac) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//    JsonObject powerStateJson = new JsonObject();
//    if (command.equals(OnOffType.ON)) { // Wake-On-(W)LAN is needed beforehand
//      int maxRetries = 5;
//      boolean isReachable = false;
//      for (int i = 1; i <= maxRetries; i++) {
//        PowerUtil.wakeOnLan(target.getHostName(), mac);
//        logger.debug("Wake-On-LAN packet number {} was successfully sent.", i);
//        Thread.sleep(DELAY_MILLIS); // let the network interface of the tv wake up
//        try {
//          isReachable = PowerUtil.isReachable(target.getHostName());
//          if(isReachable){
//            break;
//          }
//        } catch (Exception ex) {
//          // Ignore thrown exceptions
//          logger.trace("IsReachable during WOL threw following exception: {}", ex.getMessage(), ex);
//        }
//      }
//      if(isReachable) {
//        powerStateJson.addProperty("powerState", POWER_ON);
//      } else {
//        throw new ConnectTimeoutException(String.format("Maximum retries of %d for turning the TV via Wake-On-LAN on is reached!", maxRetries));
//      }
//    } else { // OFF
//      powerStateJson.addProperty("powerState", KEY_STANDBY.toString());
//      connectionService.doHttpsPost(credentials, target, KEY_CODE_PATH, powerStateJson.toString());
//    }
//  }
}
