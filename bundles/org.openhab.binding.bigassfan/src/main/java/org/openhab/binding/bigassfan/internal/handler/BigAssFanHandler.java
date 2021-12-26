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
package org.openhab.binding.bigassfan.internal.handler;

import static org.openhab.binding.bigassfan.internal.BigAssFanBindingConstants.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.bigassfan.internal.BigAssFanConfig;
import org.openhab.binding.bigassfan.internal.utils.BigAssFanConverter;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BigAssFanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BigAssFanHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BigAssFanHandler.class);

    private static final StringType LIGHT_COLOR = new StringType("COLOR");
    private static final StringType LIGHT_PRESENT = new StringType("PRESENT");

    private static final StringType OFF = new StringType("OFF");
    private static final StringType COOLING = new StringType("COOLING");
    private static final StringType HEATING = new StringType("HEATING");

    private BigAssFanConfig config;
    private String label = null;
    private String ipAddress = null;
    private String macAddress = null;

    private FanListener fanListener;

    protected Map<String, State> fanStateMap = Collections.synchronizedMap(new HashMap<>());

    public BigAssFanHandler(Thing thing, String ipv4Address) {
        super(thing);
        this.thing = thing;

        logger.debug("Creating FanListener object for {}", thing.getUID());
        fanListener = new FanListener(ipv4Address);
    }

    @Override
    public void initialize() {
        logger.debug("BigAssFanHandler for {} is initializing", thing.getUID());

        config = getConfig().as(BigAssFanConfig.class);
        logger.debug("BigAssFanHandler config for {} is {}", thing.getUID(), config);

        if (!config.isValid()) {
            logger.debug("BigAssFanHandler config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid BigAssFan config. Check configuration.");
            return;
        }
        label = config.getLabel();
        ipAddress = config.getIpAddress();
        macAddress = config.getMacAddress();

        fanListener.startFanListener();
    }

    @Override
    public void dispose() {
        logger.debug("BigAssFanHandler for {} is disposing", thing.getUID());
        fanListener.stopFanListener();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);
        if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
            handleFanPower(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            handleFanSpeed(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_AUTO)) {
            handleFanAuto(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_WHOOSH)) {
            handleFanWhoosh(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SMARTMODE)) {
            handleFanSmartmode(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_LEARN_MINSPEED)) {
            handleFanLearnSpeedMin(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_LEARN_MAXSPEED)) {
            handleFanLearnSpeedMax(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED_MIN)) {
            handleFanSpeedMin(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED_MAX)) {
            handleFanSpeedMax(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_WINTERMODE)) {
            handleFanWintermode(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_POWER)) {
            handleLightPower(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_LEVEL)) {
            handleLightLevel(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_HUE)) {
            handleLightHue(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_AUTO)) {
            handleLightAuto(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_SMARTER)) {
            handleLightSmarter(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_LEVEL_MIN)) {
            handleLightLevelMin(command);
        } else if (channelUID.getId().equals(CHANNEL_LIGHT_LEVEL_MAX)) {
            handleLightLevelMax(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SLEEP)) {
            handleSleep(command);
        } else {
            logger.debug("Received command for {} on unknown channel {}", thing.getUID(), channelUID.getId());
        }
    }

    private void handleFanPower(Command command) {
        logger.debug("Handling fan power command for {}: {}", thing.getUID(), command);

        // <mac;FAN;PWR;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";FAN;PWR;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";FAN;PWR;ON");
            }
        }
    }

    private void handleFanSpeed(Command command) {
        logger.debug("Handling fan speed command for {}: {}", thing.getUID(), command);

        // <mac;FAN;SPD;SET;0..7>
        if (command instanceof PercentType) {
            sendCommand(macAddress, ";FAN;SPD;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
        }
    }

    private void handleFanAuto(Command command) {
        logger.debug("Handling fan auto command {}", command);

        // <mac;FAN;AUTO;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";FAN;AUTO;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";FAN;AUTO;ON");
            }
        }
    }

    private void handleFanWhoosh(Command command) {
        logger.debug("Handling fan whoosh command {}", command);

        // <mac;FAN;WHOOSH;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";FAN;WHOOSH;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";FAN;WHOOSH;ON");
            }
        }
    }

    private void handleFanSmartmode(Command command) {
        logger.debug("Handling fan smartmode command {}", command);

        // <mac;SMARTMODE;SET;OFF/COOLING/HEATING>
        if (command instanceof StringType) {
            if (command.equals(OFF)) {
                sendCommand(macAddress, ";SMARTMODE;STATE;SET;OFF");
            } else if (command.equals(COOLING)) {
                sendCommand(macAddress, ";SMARTMODE;STATE;SET;COOLING");
            } else if (command.equals(HEATING)) {
                sendCommand(macAddress, ";SMARTMODE;STATE;SET;HEATING");
            } else {
                logger.debug("Unknown fan smartmode command: {}", command);
            }
        }
    }

    private void handleFanLearnSpeedMin(Command command) {
        logger.debug("Handling fan learn speed minimum command {}", command);
        // <mac;FAN;SPD;SET;MIN;0..7>
        if (command instanceof PercentType) {
            // Send min speed set command
            sendCommand(macAddress,
                    ";LEARN;MINSPEED;SET;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
            fanStateMap.put(CHANNEL_FAN_LEARN_MINSPEED, (PercentType) command);
            // Don't let max be less than min
            adjustMaxSpeed((PercentType) command, CHANNEL_FAN_LEARN_MAXSPEED, ";LEARN;MAXSPEED;");
        }
    }

    private void handleFanLearnSpeedMax(Command command) {
        logger.debug("Handling fan learn speed maximum command {}", command);
        // <mac;FAN;SPD;SET;MAX;0..7>
        if (command instanceof PercentType) {
            // Send max speed set command
            sendCommand(macAddress,
                    ";LEARN;MAXSPEED;SET;;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
            fanStateMap.put(CHANNEL_FAN_LEARN_MAXSPEED, (PercentType) command);
            // Don't let min be greater than max
            adjustMinSpeed((PercentType) command, CHANNEL_FAN_LEARN_MINSPEED, ";LEARN;MINSPEED;");
        }
    }

    private void handleFanSpeedMin(Command command) {
        logger.debug("Handling fan speed minimum command {}", command);
        // <mac;FAN;SPD;SET;MIN;0..7>
        if (command instanceof PercentType) {
            // Send min speed set command
            sendCommand(macAddress,
                    ";FAN;SPD;SET;MIN;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
            fanStateMap.put(CHANNEL_FAN_SPEED_MIN, (PercentType) command);
            // Don't let max be less than min
            adjustMaxSpeed((PercentType) command, CHANNEL_FAN_SPEED_MAX, ";FAN;SPD;SET;MAX;");
        }
    }

    private void handleFanSpeedMax(Command command) {
        logger.debug("Handling fan speed maximum command {}", command);
        // <mac;FAN;SPD;SET;MAX;0..7>
        if (command instanceof PercentType) {
            // Send max speed set command
            sendCommand(macAddress,
                    ";FAN;SPD;SET;MAX;".concat(BigAssFanConverter.percentToSpeed((PercentType) command)));
            fanStateMap.put(CHANNEL_FAN_SPEED_MAX, (PercentType) command);
            // Don't let min be greater than max
            adjustMinSpeed((PercentType) command, CHANNEL_FAN_SPEED_MIN, ";FAN;SPD;SET;MIN;");
        }
    }

    private void handleFanWintermode(Command command) {
        logger.debug("Handling fan wintermode command {}", command);

        // <mac;FAN;WINTERMODE;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";FAN;WINTERMODE;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";FAN;WINTERMODE;ON");
            }
        }
    }

    private void handleSleep(Command command) {
        logger.debug("Handling fan sleep command {}", command);

        // <mac;SLEEP;STATE;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";SLEEP;STATE;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";SLEEP;STATE;ON");
            }
        }
    }

    private void adjustMaxSpeed(PercentType command, String channelId, String commandFragment) {
        int newMin = command.intValue();
        int currentMax = PercentType.ZERO.intValue();
        if (fanStateMap.get(channelId) != null) {
            currentMax = ((PercentType) fanStateMap.get(channelId)).intValue();
        }
        if (newMin > currentMax) {
            updateState(CHANNEL_FAN_SPEED_MAX, command);
            sendCommand(macAddress, commandFragment.concat(BigAssFanConverter.percentToSpeed(command)));
        }
    }

    private void adjustMinSpeed(PercentType command, String channelId, String commandFragment) {
        int newMax = command.intValue();
        int currentMin = PercentType.HUNDRED.intValue();
        if (fanStateMap.get(channelId) != null) {
            currentMin = ((PercentType) fanStateMap.get(channelId)).intValue();
        }
        if (newMax < currentMin) {
            updateState(channelId, command);
            sendCommand(macAddress, commandFragment.concat(BigAssFanConverter.percentToSpeed(command)));
        }
    }

    private void handleLightPower(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light power command {}", command);
        // <mac;LIGHT;PWR;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";LIGHT;PWR;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";LIGHT;PWR;ON");
            }
        }
    }

    private void handleLightLevel(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light level command {}", command);
        // <mac;LIGHT;LEVEL;SET;0..16>
        if (command instanceof PercentType) {
            sendCommand(macAddress,
                    ";LIGHT;LEVEL;SET;".concat(BigAssFanConverter.percentToLevel((PercentType) command)));
        }
    }

    private void handleLightHue(Command command) {
        if (!isLightPresent() || !isLightColor()) {
            logger.debug("Fan does not have light installed or does not support hue for command {}", command);
            return;
        }

        logger.debug("Handling light hue command {}", command);
        // <mac;LIGHT;COLOR;TEMP;SET;2200..5000>
        if (command instanceof PercentType) {
            sendCommand(macAddress,
                    ";LIGHT;COLOR;TEMP;VALUE;SET;".concat(BigAssFanConverter.percentToHue((PercentType) command)));
        }
    }

    private void handleLightAuto(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light auto command {}", command);
        // <mac;LIGHT;AUTO;ON|OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";LIGHT;AUTO;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";LIGHT;AUTO;ON");
            }
        }
    }

    private void handleLightSmarter(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light smartmode command {}", command);
        // <mac;LIGHT;SMART;ON/OFF>
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.OFF)) {
                sendCommand(macAddress, ";LIGHT;SMART;OFF");
            } else if (command.equals(OnOffType.ON)) {
                sendCommand(macAddress, ";LIGHT;SMART;ON");
            }
        }
    }

    private void handleLightLevelMin(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light level minimum command {}", command);
        // <mac;LIGHT;LEVEL;MIN;0-16>
        if (command instanceof PercentType) {
            // Send min light level set command
            sendCommand(macAddress,
                    ";LIGHT;LEVEL;MIN;".concat(BigAssFanConverter.percentToLevel((PercentType) command)));
            // Don't let max be less than min
            adjustMaxLevel((PercentType) command);
        }
    }

    private void handleLightLevelMax(Command command) {
        if (!isLightPresent()) {
            logger.debug("Fan does not have light installed for command {}", command);
            return;
        }

        logger.debug("Handling light level maximum command {}", command);
        // <mac;LIGHT;LEVEL;MAX;0-16>
        if (command instanceof PercentType) {
            // Send max light level set command
            sendCommand(macAddress,
                    ";LIGHT;LEVEL;MAX;".concat(BigAssFanConverter.percentToLevel((PercentType) command)));
            // Don't let min be greater than max
            adjustMinLevel((PercentType) command);
        }
    }

    private void adjustMaxLevel(PercentType command) {
        int newMin = command.intValue();
        int currentMax = PercentType.ZERO.intValue();
        if (fanStateMap.get(CHANNEL_LIGHT_LEVEL_MAX) != null) {
            currentMax = ((PercentType) fanStateMap.get(CHANNEL_LIGHT_LEVEL_MAX)).intValue();
        }
        if (newMin > currentMax) {
            updateState(CHANNEL_LIGHT_LEVEL_MAX, command);
            sendCommand(macAddress, ";LIGHT;LEVEL;MAX;".concat(BigAssFanConverter.percentToLevel(command)));
        }
    }

    private void adjustMinLevel(PercentType command) {
        int newMax = command.intValue();
        int currentMin = PercentType.HUNDRED.intValue();
        if (fanStateMap.get(CHANNEL_LIGHT_LEVEL_MIN) != null) {
            currentMin = ((PercentType) fanStateMap.get(CHANNEL_LIGHT_LEVEL_MIN)).intValue();
        }
        if (newMax < currentMin) {
            updateState(CHANNEL_LIGHT_LEVEL_MIN, command);
            sendCommand(macAddress, ";LIGHT;LEVEL;MIN;".concat(BigAssFanConverter.percentToLevel(command)));
        }
    }

    private boolean isLightPresent() {
        return fanStateMap.containsKey(CHANNEL_LIGHT_PRESENT)
                && LIGHT_PRESENT.equals(fanStateMap.get(CHANNEL_LIGHT_PRESENT));
    }

    private boolean isLightColor() {
        return fanStateMap.containsKey(CHANNEL_LIGHT_COLOR) && LIGHT_COLOR.equals(fanStateMap.get(CHANNEL_LIGHT_COLOR));
    }

    /*
     * Send a command to the fan
     */
    private void sendCommand(String mac, String commandFragment) {
        if (fanListener == null) {
            logger.error("Unable to send message to {} because fanListener object is null!", thing.getUID());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(mac).append(commandFragment).append(">");
        String message = sb.toString();
        logger.trace("Sending message to {} at {}: {}", thing.getUID(), ipAddress, message);
        fanListener.send(message);
    }

    private void updateChannel(String channelName, State state) {
        Channel channel = thing.getChannel(channelName);
        if (channel != null) {
            updateState(channel.getUID(), state);
        }
    }

    /*
     * Manage the ONLINE/OFFLINE status of the thing
     */
    private void markOnline() {
        if (!isOnline()) {
            logger.debug("Changing status of {} from {}({}) to ONLINE", thing.getUID(), getStatus(), getDetail());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void markOffline() {
        if (isOnline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE", thing.getUID(), getStatus(), getDetail());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void markOfflineWithMessage(ThingStatusDetail statusDetail, String statusMessage) {
        // If it's offline with no detail or if it's not offline, mark it offline with detailed status
        if ((isOffline() && getDetail() == ThingStatusDetail.NONE) || !isOffline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE({})", thing.getUID(), getStatus(), getDetail(),
                    statusDetail);
            updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);
            return;
        }
    }

    private boolean isOnline() {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    private boolean isOffline() {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    private ThingStatus getStatus() {
        return thing.getStatus();
    }

    private ThingStatusDetail getDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    /**
     * The {@link FanListener} is responsible for sending and receiving messages to a fan.
     *
     * @author Mark Hilbush - Initial contribution
     */
    public class FanListener {
        private final Logger logger = LoggerFactory.getLogger(FanListener.class);

        // Our own thread pool for the long-running listener job
        private ScheduledExecutorService scheduledExecutorService = ThreadPoolManager
                .getScheduledPool("bigassfanHandler" + "-" + thing.getUID());
        private ScheduledFuture<?> listenerJob;

        private final long FAN_LISTENER_DELAY = 2L;
        private boolean terminate;

        private final Pattern messagePattern = Pattern.compile("[(](.*)");

        private ConnectionManager conn;

        private Runnable fanListenerRunnable = () -> {
            try {
                listener();
            } catch (RuntimeException e) {
                logger.warn("FanListener for {} had unhandled exception: {}", thing.getUID(), e.getMessage(), e);
            }
        };

        public FanListener(String ipv4Address) {
            conn = new ConnectionManager(ipv4Address);
        }

        public void startFanListener() {
            conn.connect();
            conn.scheduleConnectionMonitorJob();

            if (listenerJob == null) {
                terminate = false;
                logger.debug("Starting listener in {} sec for {} at {}", FAN_LISTENER_DELAY, thing.getUID(), ipAddress);
                listenerJob = scheduledExecutorService.schedule(fanListenerRunnable, FAN_LISTENER_DELAY,
                        TimeUnit.SECONDS);
            }
        }

        public void stopFanListener() {
            if (listenerJob != null) {
                logger.debug("Stopping listener for {} at {}", thing.getUID(), ipAddress);
                terminate = true;
                listenerJob.cancel(true);
                listenerJob = null;
            }
            conn.cancelConnectionMonitorJob();
            conn.disconnect();
        }

        public void send(String command) {
            if (!conn.isConnected()) {
                logger.debug("Unable to send message; no connection to {}. Trying to reconnect: {}", thing.getUID(),
                        command);
                conn.connect();
                if (!conn.isConnected()) {
                    return;
                }
            }

            logger.debug("Sending message to {} at {}: {}", thing.getUID(), ipAddress, command);
            byte[] buffer;
            try {
                buffer = command.getBytes(CHARSET);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Unable to convert to string using {} charset: {}", CHARSET, e.getMessage(), e);
                return;
            }
            try {
                conn.write(buffer);
            } catch (IOException e) {
                logger.warn("IO exception writing message to socket: {}", e.getMessage(), e);
                conn.disconnect();
            }
        }

        private void listener() {
            logger.debug("Fan listener thread is running for {} at {}", thing.getUID(), ipAddress);

            while (!terminate) {
                try {
                    // Wait for a message
                    processMessage(waitForMessage());
                } catch (IOException ioe) {
                    logger.warn("Listener for {} got IO exception waiting for message: {}", thing.getUID(),
                            ioe.getMessage(), ioe);
                    break;
                }
            }
            logger.debug("Fan listener thread is exiting for {} at {}", thing.getUID(), ipAddress);
        }

        private String waitForMessage() throws IOException {
            if (!conn.isConnected()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("FanListener for {} can't receive message. No connection to fan", thing.getUID());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                return null;
            }
            return readMessage();
        }

        private String readMessage() {
            logger.trace("Waiting for message from {}  at {}", thing.getUID(), ipAddress);
            String message = conn.read();
            if (message != null) {
                logger.trace("FanListener for {} received message of length {}: {}", thing.getUID(), message.length(),
                        message);
            }
            return message;
        }

        private void processMessage(String incomingMessage) {
            if (incomingMessage == null || incomingMessage.isEmpty()) {
                return;
            }

            // Match on (msg)
            logger.debug("FanListener for {} received message from {}: {}", thing.getUID(), macAddress,
                    incomingMessage);
            Matcher matcher = messagePattern.matcher(incomingMessage);
            if (!matcher.find()) {
                logger.debug("Unable to process message from {}, not in expected format: {}", thing.getUID(),
                        incomingMessage);
                return;
            }

            String message = matcher.group(1);
            String[] messageParts = message.split(";");

            // Check to make sure it is my MAC address or my label
            if (!isMe(messageParts[0])) {
                logger.trace("Message not for me ({}): {}", messageParts[0], macAddress);
                return;
            }

            logger.trace("Message is for me ({}): {}", messageParts[0], macAddress);
            String messageUpperCase = message.toUpperCase();
            if (messageUpperCase.contains(";FAN;PWR;")) {
                updateFanPower(messageParts);
            } else if (messageUpperCase.contains(";FAN;SPD;ACTUAL;")) {
                updateFanSpeed(messageParts);
            } else if (messageUpperCase.contains(";FAN;DIR;")) {
                updateFanDirection(messageParts);
            } else if (messageUpperCase.contains(";FAN;AUTO;")) {
                updateFanAuto(messageParts);
            } else if (messageUpperCase.contains(";FAN;WHOOSH;STATUS;")) {
                updateFanWhoosh(messageParts);
            } else if (messageUpperCase.contains(";WINTERMODE;STATE;")) {
                updateFanWintermode(messageParts);
            } else if (messageUpperCase.contains(";SMARTMODE;STATE;")) {
                updateFanSmartmode(messageParts);
            } else if (messageUpperCase.contains(";FAN;SPD;MIN;")) {
                updateFanSpeedMin(messageParts);
            } else if (messageUpperCase.contains(";FAN;SPD;MAX;")) {
                updateFanSpeedMax(messageParts);
            } else if (messageUpperCase.contains(";SLEEP;STATE")) {
                updateFanSleepMode(messageParts);
            } else if (messageUpperCase.contains(";LEARN;MINSPEED;")) {
                updateFanLearnMinSpeed(messageParts);
            } else if (messageUpperCase.contains(";LEARN;MAXSPEED;")) {
                updateFanLearnMaxSpeed(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;PWR;")) {
                updateLightPower(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;LEVEL;ACTUAL;")) {
                updateLightLevel(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;COLOR;TEMP;VALUE;")) {
                updateLightHue(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;AUTO;")) {
                updateLightAuto(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;LEVEL;MIN;")) {
                updateLightLevelMin(messageParts);
            } else if (messageUpperCase.contains(";LIGHT;LEVEL;MAX;")) {
                updateLightLevelMax(messageParts);
            } else if (messageUpperCase.contains(";DEVICE;LIGHT;")) {
                updateLightPresent(messageParts);
            } else if (messageUpperCase.contains(";SNSROCC;STATUS;")) {
                updateMotion(messageParts);
            } else if (messageUpperCase.contains(";TIME;VALUE;")) {
                updateTime(messageParts);
            } else {
                logger.trace("Received unsupported message from {}: {}", thing.getUID(), message);
            }
        }

        private boolean isMe(String idFromDevice) {
            // Check match on MAC address
            if (macAddress.equalsIgnoreCase(idFromDevice)) {
                return true;
            }
            // Didn't match MAC address, check match for label
            if (label.equalsIgnoreCase(idFromDevice)) {
                return true;
            }
            return false;
        }

        private void updateFanPower(String[] messageParts) {
            if (messageParts.length != 4) {
                if (logger.isDebugEnabled()) {
                    logger.debug("FAN;PWR has unexpected number of parameters: {}", Arrays.toString(messageParts));
                }
                return;
            }
            logger.debug("Process fan power update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_FAN_POWER, state);
            fanStateMap.put(CHANNEL_FAN_POWER, state);
        }

        private void updateFanSpeed(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("FAN;SPD;ACTUAL has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan speed update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.speedToPercent(messageParts[4]);
            updateChannel(CHANNEL_FAN_SPEED, state);
            fanStateMap.put(CHANNEL_FAN_SPEED, state);
        }

        private void updateFanDirection(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("FAN;DIR has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan direction update for {}: {}", thing.getUID(), messageParts[3]);
            StringType state = new StringType(messageParts[3]);
            updateChannel(CHANNEL_FAN_DIRECTION, state);
            fanStateMap.put(CHANNEL_FAN_DIRECTION, state);
        }

        private void updateFanAuto(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("FAN;AUTO has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan auto update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_FAN_AUTO, state);
            fanStateMap.put(CHANNEL_FAN_AUTO, state);
        }

        private void updateFanWhoosh(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("FAN;WHOOSH has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan whoosh update for {}: {}", thing.getUID(), messageParts[4]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[4]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_FAN_WHOOSH, state);
            fanStateMap.put(CHANNEL_FAN_WHOOSH, state);
        }

        private void updateFanWintermode(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("WINTERMODE;STATE has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan wintermode update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_FAN_WINTERMODE, state);
            fanStateMap.put(CHANNEL_FAN_WINTERMODE, state);
        }

        private void updateFanSmartmode(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("Smartmode has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan smartmode update for {}: {}", thing.getUID(), messageParts[3]);
            StringType state = new StringType(messageParts[3]);
            updateChannel(CHANNEL_FAN_SMARTMODE, state);
            fanStateMap.put(CHANNEL_FAN_SMARTMODE, state);
        }

        private void updateFanSpeedMin(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("FanSpeedMin has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan min speed update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.speedToPercent(messageParts[4]);
            updateChannel(CHANNEL_FAN_SPEED_MIN, state);
            fanStateMap.put(CHANNEL_FAN_SPEED_MIN, state);
        }

        private void updateFanSpeedMax(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("FanSpeedMax has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan speed max update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.speedToPercent(messageParts[4]);
            updateChannel(CHANNEL_FAN_SPEED_MAX, state);
            fanStateMap.put(CHANNEL_FAN_SPEED_MAX, state);
        }

        private void updateFanSleepMode(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("SLEEP;STATE; has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan sleep mode for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_FAN_SLEEP, state);
            fanStateMap.put(CHANNEL_FAN_SLEEP, state);
        }

        private void updateFanLearnMinSpeed(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("FanLearnMaxSpeed has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan learn min speed update for {}: {}", thing.getUID(), messageParts[3]);
            PercentType state = BigAssFanConverter.speedToPercent(messageParts[3]);
            updateChannel(CHANNEL_FAN_LEARN_MINSPEED, state);
            fanStateMap.put(CHANNEL_FAN_LEARN_MINSPEED, state);
        }

        private void updateFanLearnMaxSpeed(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("FanLearnMaxSpeed has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process fan learn max speed update for {}: {}", thing.getUID(), messageParts[3]);
            PercentType state = BigAssFanConverter.speedToPercent(messageParts[3]);
            updateChannel(CHANNEL_FAN_LEARN_MAXSPEED, state);
            fanStateMap.put(CHANNEL_FAN_LEARN_MAXSPEED, state);
        }

        private void updateLightPower(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("LIGHT;PWR has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light power update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_LIGHT_POWER, state);
            fanStateMap.put(CHANNEL_LIGHT_POWER, state);
        }

        private void updateLightLevel(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("LIGHT;LEVEL has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light level update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.levelToPercent(messageParts[4]);
            updateChannel(CHANNEL_LIGHT_LEVEL, state);
            fanStateMap.put(CHANNEL_LIGHT_LEVEL, state);
        }

        private void updateLightHue(String[] messageParts) {
            if (messageParts.length != 6) {
                logger.debug("LIGHT;COLOR;TEMP;VALUE has unexpected number of parameters: {}",
                        Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light hue update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.hueToPercent(messageParts[5]);
            updateChannel(CHANNEL_LIGHT_HUE, state);
            fanStateMap.put(CHANNEL_LIGHT_HUE, state);
        }

        private void updateLightAuto(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("LIGHT;AUTO has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light auto update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "ON".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_LIGHT_AUTO, state);
            fanStateMap.put(CHANNEL_LIGHT_AUTO, state);
        }

        private void updateLightLevelMin(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("LightLevelMin has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light level min update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.levelToPercent(messageParts[4]);
            updateChannel(CHANNEL_LIGHT_LEVEL_MIN, state);
            fanStateMap.put(CHANNEL_LIGHT_LEVEL_MIN, state);
        }

        private void updateLightLevelMax(String[] messageParts) {
            if (messageParts.length != 5) {
                logger.debug("LightLevelMax has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light level max update for {}: {}", thing.getUID(), messageParts[4]);
            PercentType state = BigAssFanConverter.levelToPercent(messageParts[4]);
            updateChannel(CHANNEL_LIGHT_LEVEL_MAX, state);
            fanStateMap.put(CHANNEL_LIGHT_LEVEL_MAX, state);
        }

        private void updateLightPresent(String[] messageParts) {
            if (messageParts.length < 4) {
                logger.debug("LightPresent has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process light present update for {}: {}", thing.getUID(), messageParts[3]);
            StringType lightPresent = new StringType(messageParts[3]);
            updateChannel(CHANNEL_LIGHT_PRESENT, lightPresent);
            fanStateMap.put(CHANNEL_LIGHT_PRESENT, lightPresent);
            if (messageParts.length == 5) {
                logger.debug("Light supports hue adjustment");
                StringType lightColor = new StringType(messageParts[4]);
                updateChannel(CHANNEL_LIGHT_COLOR, lightColor);
                fanStateMap.put(CHANNEL_LIGHT_COLOR, lightColor);
            }
        }

        private void updateMotion(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("SNSROCC has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process motion sensor update for {}: {}", thing.getUID(), messageParts[3]);
            OnOffType state = "OCCUPIED".equalsIgnoreCase(messageParts[3]) ? OnOffType.ON : OnOffType.OFF;
            updateChannel(CHANNEL_MOTION, state);
            fanStateMap.put(CHANNEL_MOTION, state);
        }

        private void updateTime(String[] messageParts) {
            if (messageParts.length != 4) {
                logger.debug("TIME has unexpected number of parameters: {}", Arrays.toString(messageParts));
                return;
            }
            logger.debug("Process time update for {}: {}", thing.getUID(), messageParts[3]);
            // (mac|name;TIME;VALUE;2017-03-26T14:06:27Z)
            try {
                Instant instant = Instant.parse(messageParts[3]);
                DateTimeType state = new DateTimeType(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
                updateChannel(CHANNEL_TIME, state);
                fanStateMap.put(CHANNEL_TIME, state);
            } catch (DateTimeParseException e) {
                logger.info("Failed to parse date received from {}: {}", thing.getUID(), messageParts[3]);
            }
        }
    }

    /*
     * The {@link ConnectionManager} class is responsible for managing the state of the TCP connection to the
     * fan.
     *
     * @author Mark Hilbush - Initial contribution
     */
    private class ConnectionManager {
        private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

        private boolean deviceIsConnected;

        private InetAddress ifAddress;
        private Socket fanSocket;
        private Scanner fanScanner;
        private DataOutputStream fanWriter;
        private final int SOCKET_CONNECT_TIMEOUT = 1500;

        ScheduledFuture<?> connectionMonitorJob;
        private final long CONNECTION_MONITOR_FREQ = 120L;
        private final long CONNECTION_MONITOR_DELAY = 30L;

        Runnable connectionMonitorRunnable = () -> {
            logger.trace("Performing connection check for {} at IP {}", thing.getUID(), ipAddress);
            checkConnection();
        };

        public ConnectionManager(String ipv4Address) {
            deviceIsConnected = false;
            try {
                ifAddress = InetAddress.getByName(ipv4Address);
                logger.debug("Handler for {} using address {} on network interface {}", thing.getUID(),
                        ifAddress.getHostAddress(), NetworkInterface.getByInetAddress(ifAddress).getName());
            } catch (UnknownHostException e) {
                logger.warn("Handler for {} got UnknownHostException getting local IPv4 net interface: {}",
                        thing.getUID(), e.getMessage(), e);
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No suitable network interface");
            } catch (SocketException e) {
                logger.warn("Handler for {} got SocketException getting local IPv4 network interface: {}",
                        thing.getUID(), e.getMessage(), e);
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No suitable network interface");
            }
        }

        /*
         * Connect to the command and serial port(s) on the device. The serial connections are established only for
         * devices that support serial.
         */
        protected synchronized void connect() {
            if (isConnected()) {
                return;
            }
            logger.trace("Connecting to {} at {}", thing.getUID(), ipAddress);

            // Open socket
            try {
                fanSocket = new Socket();
                fanSocket.bind(new InetSocketAddress(ifAddress, 0));
                fanSocket.connect(new InetSocketAddress(ipAddress, BAF_PORT), SOCKET_CONNECT_TIMEOUT);
            } catch (IOException e) {
                logger.debug("IOException connecting to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
                return;
            }

            // Create streams
            try {
                fanWriter = new DataOutputStream(fanSocket.getOutputStream());
                fanScanner = new Scanner(fanSocket.getInputStream());
                fanScanner.useDelimiter("[)]");
            } catch (IOException e) {
                logger.warn("IOException getting streams for {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
                return;
            }
            logger.info("Connected to {} at {}", thing.getUID(), ipAddress);
            deviceIsConnected = true;
            markOnline();
        }

        protected synchronized void disconnect() {
            if (!isConnected()) {
                return;
            }
            logger.debug("Disconnecting from {} at {}", thing.getUID(), ipAddress);

            try {
                if (fanWriter != null) {
                    fanWriter.close();
                }
                if (fanScanner != null) {
                    fanScanner.close();
                }
                if (fanSocket != null) {
                    fanSocket.close();
                }
            } catch (IOException e) {
                logger.warn("IOException closing connection to {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
            }
            deviceIsConnected = false;
            fanSocket = null;
            fanScanner = null;
            fanWriter = null;
            markOffline();
        }

        public String read() {
            if (fanScanner == null) {
                logger.warn("Scanner for {} is null when trying to scan from {}!", thing.getUID(), ipAddress);
                return null;
            }

            String nextToken;
            try {
                nextToken = fanScanner.next();
            } catch (NoSuchElementException e) {
                logger.debug("Scanner for {} threw NoSuchElementException; stream possibly closed", thing.getUID());
                // Force a reconnect to the device
                disconnect();
                nextToken = null;
            } catch (IllegalStateException e) {
                logger.debug("Scanner for {} threw IllegalStateException; scanner possibly closed", thing.getUID());
                nextToken = null;
            } catch (BufferOverflowException e) {
                logger.debug("Scanner for {} threw BufferOverflowException", thing.getUID());
                nextToken = null;
            }
            return nextToken;
        }

        public void write(byte[] buffer) throws IOException {
            if (fanWriter == null) {
                logger.warn("fanWriter for {} is null when trying to write to {}!!!", thing.getUID(), ipAddress);
                return;
            }
            fanWriter.write(buffer, 0, buffer.length);
        }

        private boolean isConnected() {
            return deviceIsConnected;
        }

        /*
         * Periodically validate the command connection to the device by executing a getversion command.
         */
        private void scheduleConnectionMonitorJob() {
            if (connectionMonitorJob == null) {
                logger.debug("Starting connection monitor job in {} seconds for {} at {}", CONNECTION_MONITOR_DELAY,
                        thing.getUID(), ipAddress);
                connectionMonitorJob = scheduler.scheduleWithFixedDelay(connectionMonitorRunnable,
                        CONNECTION_MONITOR_DELAY, CONNECTION_MONITOR_FREQ, TimeUnit.SECONDS);
            }
        }

        private void cancelConnectionMonitorJob() {
            if (connectionMonitorJob != null) {
                logger.debug("Canceling connection monitor job for {} at {}", thing.getUID(), ipAddress);
                connectionMonitorJob.cancel(true);
                connectionMonitorJob = null;
            }
        }

        private void checkConnection() {
            logger.trace("Checking status of connection for {} at {}", thing.getUID(), ipAddress);
            if (!isConnected()) {
                logger.debug("Connection check FAILED for {} at {}", thing.getUID(), ipAddress);
                connect();
            } else {
                logger.debug("Connection check OK for {} at {}", thing.getUID(), ipAddress);
                logger.debug("Requesting status update from {} at {}", thing.getUID(), ipAddress);
                sendCommand(macAddress, ";GETALL");
                sendCommand(macAddress, ";SNSROCC;STATUS;GET");
            }
        }
    }
}
