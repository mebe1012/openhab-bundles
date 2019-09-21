/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.service;

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
import org.openhab.binding.philipstv.internal.service.model.PowerStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
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
                PowerStateDto powerStateDto = getPowerState();
                if (powerStateDto.isPoweredOn()) {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                } else {
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

    private PowerStateDto getPowerState() throws IOException, ParseException, JsonSyntaxException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_POWERSTATE_PATH), PowerStateDto.class);
    }

    private void setPowerState(Command command) throws IOException {
        PowerStateDto powerStateDto = new PowerStateDto();
        if (command.equals(OnOffType.ON)) {
            powerStateDto.setPowerState(POWER_ON);
        } else { // OFF
            powerStateDto.setPowerState(KEY_STANDBY.toString());
        }
        String powerStateJson = OBJECT_MAPPER.writeValueAsString(powerStateDto);
        logger.debug("PowerState Json sent: {}", powerStateJson);
        connectionManager.doHttpsPost(TV_POWERSTATE_PATH, powerStateJson);
    }
}
