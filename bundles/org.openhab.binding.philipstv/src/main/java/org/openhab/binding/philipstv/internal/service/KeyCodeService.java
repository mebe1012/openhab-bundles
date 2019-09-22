/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.service;

import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.openhab.binding.philipstv.internal.service.model.KeyCode.KeyCodeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.KEY_CODE_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

/**
 * The {@link KeyCodeService} is responsible for handling key code commands, which emulate a button
 * press on a remote control.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class KeyCodeService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTvHandler handler;

    private final ConnectionManager connectionManager;

    public KeyCodeService(PhilipsTvHandler handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        KeyCode keyCode = null;
        if (isSupportedCommand(command)) {
            // Three approaches to resolve the KEY_CODE
            try {
                keyCode = KeyCode.valueOf(command.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    keyCode = KeyCode.valueOf("KEY_" + command.toString().toUpperCase());
                } catch (IllegalArgumentException e2) {
                    try {
                        keyCode = KeyCode.getKeyCodeForValue(command.toString());
                    } catch (IllegalArgumentException e3) {
                        // do nothing, error message is logged later
                    }
                }
            }

            if (keyCode != null) {
                try {
                    sendKeyCode(keyCode);
                } catch (Exception e) {
                    if (isTvOfflineException(e)) {
                        logger.warn("Could not execute command for key code, the TV is offline.");
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
                    } else if (isTvNotListeningException(e)) {
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                TV_NOT_LISTENING_MSG);
                    } else {
                        logger.warn("Unknown error occurred while sending keyCode code {}: {}", keyCode, e.getMessage(),
                                e);
                    }
                }
            } else {
                logger.warn("Command '{}' not a supported keyCode code.", command);
            }
        } else {
            if (!(command instanceof RefreshType)) { // RefreshType is valid but ignored
                logger.warn("Not a supported command: {}", command);
            }
        }
    }

    private static boolean isSupportedCommand(Command command) {
        return (command instanceof StringType) || (command instanceof NextPreviousType) ||
                (command instanceof PlayPauseType) || (command instanceof RewindFastforwardType);
    }

    private void sendKeyCode(KeyCode key) throws IOException {
        KeyCodeDto keyCodeDto = new KeyCodeDto();
        keyCodeDto.setKey(key);
        String keyCodeJson = OBJECT_MAPPER.writeValueAsString(keyCodeDto);
        logger.debug("KeyCode Json sent: {}", keyCodeJson);
        connectionManager.doHttpsPost(KEY_CODE_PATH, keyCodeJson);
    }
}
