package org.openhab.binding.philipstv.internal.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_POWERSTATE_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_HUE_POWER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_POWER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_OFF;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.UPDATE_SETTINGS_PATH;

public class AmbilightService implements PhilipsTvService {

    public static final String AMBILIGHT_HUE_NODE_ID = "2131230774";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionService connectionService = new ConnectionService();

    @Override
    public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
        try {
            if (CHANNEL_AMBILIGHT_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightPowerState(command);
            } else if (CHANNEL_AMBILIGHT_HUE_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightHuePowerState(command);
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

    private void setAmbilightPowerState(Command command) throws IOException {
        JsonObject powerStateJson = new JsonObject();
        if (command.equals(OnOffType.ON)) {
            powerStateJson.addProperty("power", POWER_ON);
        } else { // OFF
            powerStateJson.addProperty("power", POWER_OFF);
        }
        connectionService.doHttpsPost(AMBILIGHT_POWERSTATE_PATH, powerStateJson.toString());
    }

    private void setAmbilightHuePowerState(Command command) throws IOException {
        JsonObject values = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        JsonObject valueJson = new JsonObject();
        JsonObject valueContent = new JsonObject();
        valueContent.addProperty("Nodeid", AMBILIGHT_HUE_NODE_ID);
        valueContent.addProperty("Controllable", "true");
        valueContent.addProperty("Available", "true");
        JsonObject data = new JsonObject();
        if (command.equals(OnOffType.ON)) {
            data.addProperty("value", "true");
        } else { // OFF
            data.addProperty("value", "false");
        }
        valueContent.add("data", data);
        valueJson.add("value", valueContent);
        jsonArray.add(valueJson);
        values.add("values", jsonArray);
        connectionService.doHttpsPost(UPDATE_SETTINGS_PATH, values.toString());
    }
}