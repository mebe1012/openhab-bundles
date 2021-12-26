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

package org.openhab.binding.mqtt.espmilighthub.internal.handler;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;
import static org.openhab.binding.mqtt.espmilighthub.internal.EspMilightHubBindingConstants.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.espmilighthub.internal.ConfigOptions;
import org.openhab.binding.mqtt.espmilighthub.internal.Helper;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EspMilightHubHandler} is responsible for handling commands of the globes, which are then
 * sent to one of the bridges to be sent out by MQTT.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class EspMilightHubHandler extends BaseThingHandler implements MqttConnectionObserver, MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private @Nullable MqttBrokerConnection connection;
    private ThingRegistry thingRegistry;
    private String globeType = "";
    private String bulbMode = "";
    private String remotesGroupID = "";
    private String channelPrefix = "";
    private String fullCommandTopic = "";
    private String fullStatesTopic = "";
    private BigDecimal maxColourTemp = BigDecimal.ZERO;
    private BigDecimal minColourTemp = BigDecimal.ZERO;
    private BigDecimal savedLevel = BIG_DECIMAL_100;
    private ConfigOptions config = new ConfigOptions();

    public EspMilightHubHandler(Thing thing, ThingRegistry thingRegistry) {
        super(thing);
        this.thingRegistry = thingRegistry;
    }

    void changeChannel(String channel, State state) {
        updateState(new ChannelUID(channelPrefix + channel), state);
        // Remote code of 0 means that all channels need to follow this change.
        if ("0".equals(remotesGroupID)) {
            switch (globeType) {
                // These two are 8 channel remotes
                case "fut091":
                case "fut089":
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "5:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "6:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "7:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "8:" + channel),
                            state);
                default:
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "1:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "2:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "3:" + channel),
                            state);
                    updateState(new ChannelUID(channelPrefix.substring(0, channelPrefix.length() - 2) + "4:" + channel),
                            state);
            }
        }
    }

    private void processIncomingState(String messageJSON) {
        // Need to handle State and Level at the same time to process level=0 as off//
        BigDecimal tempBulbLevel = BigDecimal.ZERO;
        String bulbState = Helper.resolveJSON(messageJSON, "\"state\":\"", 3);
        String bulbLevel = Helper.resolveJSON(messageJSON, "\"level\":", 3);
        if (!bulbLevel.isEmpty()) {
            if ("0".equals(bulbLevel) || "OFF".equals(bulbState)) {
                changeChannel(CHANNEL_LEVEL, OnOffType.OFF);
                tempBulbLevel = BigDecimal.ZERO;
            } else {
                tempBulbLevel = new BigDecimal(bulbLevel);
                changeChannel(CHANNEL_LEVEL, new PercentType(tempBulbLevel));
            }
        } else if ("ON".equals(bulbState) || "OFF".equals(bulbState)) { // NOTE: Level is missing when this runs
            changeChannel(CHANNEL_LEVEL, OnOffType.valueOf(bulbState));
        }
        bulbMode = Helper.resolveJSON(messageJSON, "\"bulb_mode\":\"", 5);
        switch (bulbMode) {
            case "white":
                if (!"cct".equals(globeType) && !"fut091".equals(globeType)) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("white"));
                    changeChannel(CHANNEL_COLOUR, new HSBType("0,0," + tempBulbLevel));
                    changeChannel(CHANNEL_DISCO_MODE, new StringType("None"));
                }
                String bulbCTemp = Helper.resolveJSON(messageJSON, "\"color_temp\":", 3);
                if (!bulbCTemp.isEmpty()) {
                    int ibulbCTemp = (int) Math.round(((Float.valueOf(bulbCTemp) / 2.17) - 171) * -1);
                    changeChannel(CHANNEL_COLOURTEMP, new PercentType(ibulbCTemp));
                }
                break;
            case "color":
                changeChannel(CHANNEL_BULB_MODE, new StringType("color"));
                changeChannel(CHANNEL_DISCO_MODE, new StringType("None"));
                String bulbHue = Helper.resolveJSON(messageJSON, "\"hue\":", 3);
                String bulbSaturation = Helper.resolveJSON(messageJSON, "\"saturation\":", 3);
                if (bulbHue.isEmpty()) {
                    logger.warn("Milight MQTT message came in as being a colour mode, but was missing a HUE value.");
                } else {
                    if (bulbSaturation.isEmpty()) {
                        bulbSaturation = "100";
                    }
                    changeChannel(CHANNEL_COLOUR, new HSBType(bulbHue + "," + bulbSaturation + "," + tempBulbLevel));
                }
                break;
            case "scene":
                if (!"cct".equals(globeType) && !"fut091".equals(globeType)) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("scene"));
                }
                String bulbDiscoMode = Helper.resolveJSON(messageJSON, "\"mode\":", 1);
                if (!bulbDiscoMode.isEmpty()) {
                    changeChannel(CHANNEL_DISCO_MODE, new StringType(bulbDiscoMode));
                }
                break;
            case "night":
                if (!"cct".equals(globeType) && !"fut091".equals(globeType)) {
                    changeChannel(CHANNEL_BULB_MODE, new StringType("night"));
                    if (config.oneTriggersNightMode) {
                        changeChannel(CHANNEL_LEVEL, new PercentType("1"));
                    }
                }
                break;
        }
    }

    /*
     * Used to calculate the colour temp for a globe if you want the light to get warmer as it is dimmed like a
     * traditional halogen globe
     */
    private int autoColourTemp(int brightness) {
        return minColourTemp.subtract(
                minColourTemp.subtract(maxColourTemp).divide(BIG_DECIMAL_100).multiply(new BigDecimal(brightness)))
                .intValue();
    }

    void turnOff() {
        if (config.powerFailsToMinimum) {
            sendMQTT("{\"state\":\"OFF\",\"level\":0}");
        } else {
            sendMQTT("{\"state\":\"OFF\"}");
        }
    }

    void handleLevelColour(Command command) {
        if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel + "}");
                return;
            } else {
                turnOff();
            }
        } else if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                if (savedLevel.intValue() <= 90) {
                    savedLevel = savedLevel.add(BigDecimal.TEN);
                }
            } else {
                if (savedLevel.intValue() >= 10) {
                    savedLevel = savedLevel.subtract(BigDecimal.TEN);
                }
            }
            sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel.intValue() + "}");
            return;
        } else if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            // This feature allows google home or Echo to trigger white mode when asked to turn color to white.
            if (hsb.getHue().intValue() == config.whiteHue && hsb.getSaturation().intValue() == config.whiteSat) {
                if ("rgb_cct".equals(globeType) || "fut089".equals(globeType)) {
                    sendMQTT("{\"state\":\"ON\",\"color_temp\":" + config.favouriteWhite + "}");
                } else {// globe must only have 1 type of white
                    sendMQTT("{\"command\":\"set_white\"}");
                }
                return;
            } else if (PercentType.ZERO.equals(hsb.getBrightness())) {
                turnOff();
                return;
            } else if (config.whiteThreshold != -1 && hsb.getSaturation().intValue() <= config.whiteThreshold) {
                sendMQTT("{\"command\":\"set_white\"}");// Can't send the command and level in the same message.
                sendMQTT("{\"level\":" + hsb.getBrightness().intValue() + "}");
            } else {
                sendMQTT("{\"state\":\"ON\",\"level\":" + hsb.getBrightness().intValue() + ",\"hue\":"
                        + hsb.getHue().intValue() + ",\"saturation\":" + hsb.getSaturation().intValue() + "}");
            }
            savedLevel = hsb.getBrightness().toBigDecimal();
            return;
        } else if (command instanceof PercentType) {
            PercentType percentType = (PercentType) command;
            if (percentType.intValue() == 0) {
                turnOff();
                return;
            } else if (percentType.intValue() == 1 && config.oneTriggersNightMode) {
                sendMQTT("{\"command\":\"night_mode\"}");
                return;
            }
            sendMQTT("{\"state\":\"ON\",\"level\":" + command + "}");
            savedLevel = percentType.toBigDecimal();
            if ("rgb_cct".equals(globeType) || "fut089".equals(globeType)) {
                if (config.dimmedCT > 0 && "white".equals(bulbMode)) {
                    sendMQTT("{\"state\":\"ON\",\"color_temp\":" + autoColourTemp(savedLevel.intValue()) + "}");
                }
            }
            return;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_LEVEL:
                handleLevelColour(command);
                return;
            case CHANNEL_BULB_MODE:
                bulbMode = command.toString();
                break;
            case CHANNEL_COLOURTEMP:
                int scaledCommand = (int) Math.round((370 - (2.17 * Float.valueOf(command.toString()))));
                sendMQTT("{\"state\":\"ON\",\"level\":" + savedLevel + ",\"color_temp\":" + scaledCommand + "}");
                break;
            case CHANNEL_COMMAND:
                sendMQTT("{\"command\":\"" + command + "\"}");
                break;
            case CHANNEL_DISCO_MODE:
                sendMQTT("{\"mode\":\"" + command + "\"}");
                break;
            case CHANNEL_COLOUR:
                handleLevelColour(command);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ConfigOptions.class);
        if (config.dimmedCT > 0) {
            maxColourTemp = new BigDecimal(config.favouriteWhite);
            minColourTemp = new BigDecimal(config.dimmedCT);
            if (minColourTemp.intValue() <= maxColourTemp.intValue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "The dimmedCT config value must be greater than the favourite White value.");
                return;
            }
        }
        Bridge localBridge = getBridge();
        if (localBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Globe must have a valid bridge selected before it can come online.");
            return;
        } else {
            globeType = thing.getThingTypeUID().getId();// eg rgb_cct
            String globeLocation = this.getThing().getUID().getId();// eg 0x014
            remotesGroupID = globeLocation.substring(globeLocation.length() - 1, globeLocation.length());// eg 4
            String remotesIDCode = globeLocation.substring(0, globeLocation.length() - 1);// eg 0x01
            fullCommandTopic = COMMANDS_BASE_TOPIC + remotesIDCode + "/" + globeType + "/" + remotesGroupID;
            fullStatesTopic = STATES_BASE_TOPIC + remotesIDCode + "/" + globeType + "/" + remotesGroupID;
            // Need to remove the lowercase x from 0x12AB in case it contains all numbers
            String caseCheck = globeLocation.substring(2, globeLocation.length() - 1);
            if (!caseCheck.equals(caseCheck.toUpperCase())) {
                logger.warn(
                        "The milight globe {}{} is using lowercase for the remote code when the hub needs UPPERCASE",
                        remotesIDCode, remotesGroupID);
            }
            channelPrefix = BINDING_ID + ":" + globeType + ":" + localBridge.getUID().getId() + ":" + remotesIDCode
                    + remotesGroupID + ":";
            connectMQTT();
        }
    }

    private void sendMQTT(String payload) {
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.publish(fullCommandTopic, payload.getBytes(), 1, false);
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String state = new String(payload, StandardCharsets.UTF_8);
        logger.trace("Recieved the following new Milight state:{}:{}", topic, state);
        processIncomingState(state);
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        logger.debug("MQTT brokers state changed to:{}", state);
        switch (state) {
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                break;
            case CONNECTING:
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Bridge (broker) is not connected to your MQTT broker.");
        }
    }

    public void connectMQTT() {
        Bridge localBridge = this.getBridge();
        if (localBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge is missing or offline, you need to setup a working MQTT broker first.");
            return;
        }
        ThingUID thingUID = localBridge.getUID();
        Thing thing = thingRegistry.get(thingUID);
        if (thing == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge is missing or offline, you need to setup a working MQTT broker first.");
            return;
        }
        ThingHandler handler = thing.getHandler();
        if (handler instanceof AbstractBrokerHandler) {
            AbstractBrokerHandler abh = (AbstractBrokerHandler) handler;
            MqttBrokerConnection localConnection = abh.getConnection();
            if (localConnection != null) {
                localConnection.setKeepAliveInterval(20);
                localConnection.setQos(1);
                localConnection.setUnsubscribeOnStop(true);
                localConnection.addConnectionObserver(this);
                localConnection.start();
                localConnection.subscribe(fullStatesTopic + "/#", this);
                connection = localConnection;
                if (localConnection.connectionState().compareTo(MqttConnectionState.CONNECTED) == 0) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }
        return;
    }

    @Override
    public void dispose() {
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.unsubscribe(fullStatesTopic + "/#", this);
        }
    }
}
