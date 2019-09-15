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
import org.apache.http.ParseException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_OFF;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_POWERSTATE_PATH;
import static org.openhab.binding.philipstv.internal.service.KeyCode.KEY_STANDBY;

/**
 * The {@link PowerService} is responsible for handling power states commands, which are sent to the
 * power channel.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class PowerService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionManager connectionManager;

    public PowerService(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
        try {
            if (command instanceof RefreshType) {
                String powerState = getPowerState();
                if (powerState.equals(POWER_ON)) {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                } else if (powerState.equals(POWER_OFF)) {
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
                }
            } else if (command instanceof OnOffType) {
                setPowerState(command);
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
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Unexpected Error handling the PowerState command {} for Channel {}: {}", command, channel,
                        e.getMessage(), e);
            }
        }
    }

    private String getPowerState() throws IOException, ParseException, JsonSyntaxException {
        String jsonContent = connectionManager.doHttpsGet(TV_POWERSTATE_PATH);
        JsonObject jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();
        String powerState = jsonObject.get("powerstate").getAsString();
        return powerState.equalsIgnoreCase(POWER_ON) ? POWER_ON : POWER_OFF;
    }

    private void setPowerState(Command command) throws IOException {
        JsonObject powerStateJson = new JsonObject();
        if (command.equals(OnOffType.ON)) {
            powerStateJson.addProperty("powerstate", POWER_ON);
        } else { // OFF
            powerStateJson.addProperty("powerstate", KEY_STANDBY.toString());
        }
        connectionManager.doHttpsPost(TV_POWERSTATE_PATH, powerStateJson.toString());
    }
}
