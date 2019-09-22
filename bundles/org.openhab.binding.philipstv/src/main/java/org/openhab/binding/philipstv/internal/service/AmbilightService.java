package org.openhab.binding.philipstv.internal.service;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.AmbilightConfigDto;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.AmbilightHuePowerDto;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.AmbilightPowerDto;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.DataDto;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.ValueDto;
import org.openhab.binding.philipstv.internal.service.model.Ambilight.ValuesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

import static org.openhab.binding.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_CONFIG_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_POWERSTATE_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_HUE_POWER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_POWER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_STYLE;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_OFF;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.UPDATE_SETTINGS_PATH;

public class AmbilightService implements PhilipsTvService {

    private static final int AMBILIGHT_HUE_NODE_ID = 2131230774;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTvHandler handler;

    private final ConnectionManager connectionManager;

    public AmbilightService(PhilipsTvHandler handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (CHANNEL_AMBILIGHT_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightPowerState(command);
            } else if (CHANNEL_AMBILIGHT_POWER.equals(channel) && (command instanceof RefreshType)) {
                AmbilightPowerDto ambilightPowerDto = getAmbilightPowerState();
                handler.postUpdateChannel(CHANNEL_AMBILIGHT_POWER,
                        ambilightPowerDto.isPoweredOn() ? OnOffType.ON : OnOffType.OFF);
            } else if (CHANNEL_AMBILIGHT_HUE_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightHuePowerState(command);
            } else if (CHANNEL_AMBILIGHT_STYLE.equals(channel) && (command instanceof StringType)) {
                setAmbilightStyle(command);
            } else {
                if (!(command instanceof RefreshType)) {
                    logger.warn("Unknown command: {} for Channel {}", command, channel);
                }
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error during handling the Ambilight command {} for Channel {}: {}", command, channel,
                        e.getMessage(), e);
            }
        }
    }

    private AmbilightPowerDto getAmbilightPowerState() throws IOException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_POWERSTATE_PATH),
                AmbilightPowerDto.class);
    }

    private void setAmbilightPowerState(Command command) throws IOException {
        AmbilightPowerDto ambilightPowerDto = new AmbilightPowerDto();
        ambilightPowerDto.setPower(command.equals(OnOffType.ON) ? POWER_ON : POWER_OFF);

        String powerStateJson = OBJECT_MAPPER.writeValueAsString(ambilightPowerDto);
        logger.debug("Post Ambilight power state json: {}", powerStateJson);
        connectionManager.doHttpsPost(AMBILIGHT_POWERSTATE_PATH, powerStateJson);
    }

    private void setAmbilightHuePowerState(Command command) throws IOException {
        AmbilightHuePowerDto ambilightHuePowerDto = new AmbilightHuePowerDto();
        ValuesDto valuesDto = new ValuesDto();

        ValueDto valueDto = new ValueDto();
        valueDto.setNodeid(AMBILIGHT_HUE_NODE_ID);
        valueDto.setAvailable("true");
        valueDto.setControllable("true");

        DataDto dataDto = new DataDto();
        dataDto.setValue(command.equals(OnOffType.ON) ? "true" : "false");

        valueDto.setData(dataDto);
        valuesDto.setValue(valueDto);
        ambilightHuePowerDto.setValues(Collections.singletonList(valuesDto));

        String ambilightHuePowerJson = OBJECT_MAPPER.writeValueAsString(ambilightHuePowerDto);
        logger.debug("Post Ambilight hue power state json: {}", ambilightHuePowerJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, ambilightHuePowerJson);
    }

    private void setAmbilightStyle(Command command) throws IOException {
        AmbilightConfigDto ambilightConfigDto = getAmbilightConfig();
        ambilightConfigDto.setStyleName(command.toString());
        if (ambilightConfigDto.getMenuSetting() == null) { // property is missing for style OFF
            ambilightConfigDto.setMenuSetting("STANDARD");
        }
        String ambilightConfigJson = OBJECT_MAPPER.writeValueAsString(ambilightConfigDto);
        logger.debug("Post config for Ambilight style json: {}", ambilightConfigJson);
        connectionManager.doHttpsPost(AMBILIGHT_CONFIG_PATH, ambilightConfigJson);
    }

    private AmbilightConfigDto getAmbilightConfig() throws IOException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_CONFIG_PATH), AmbilightConfigDto.class);
    }

}