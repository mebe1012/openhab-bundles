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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.glassfish.jersey.client.ClientProperties;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.protocol.ChargeState;
import org.openhab.binding.tesla.internal.protocol.ClimateState;
import org.openhab.binding.tesla.internal.protocol.DriveState;
import org.openhab.binding.tesla.internal.protocol.GUIState;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
import org.openhab.binding.tesla.internal.protocol.TokenRequestPassword;
import org.openhab.binding.tesla.internal.protocol.TokenRequestRefreshToken;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleState;
import org.openhab.binding.tesla.internal.throttler.QueueChannelThrottler;
import org.openhab.binding.tesla.internal.throttler.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 */
public class TeslaHandler extends BaseThingHandler {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3000;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200000;
    private static final int EVENT_TIMESTAMP_AGE_LIMIT = 3000;
    private static final int EVENT_TIMESTAMP_MAX_DELTA = 10000;
    private static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    private static final int SLOW_STATUS_REFRESH_INTERVAL = 60000;
    private static final int CONNECT_RETRY_INTERVAL = 15000;
    private static final int API_MAXIMUM_ERRORS_IN_INTERVAL = 2;
    private static final int API_ERROR_INTERVAL_SECONDS = 15;
    private static final int EVENT_MAXIMUM_ERRORS_IN_INTERVAL = 10;
    private static final int EVENT_ERROR_INTERVAL_SECONDS = 15;
    private static final int API_SLEEP_INTERVAL_MINUTES = 15;
    private static final int MOVE_THRESHOLD_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(TeslaHandler.class);

    // Vehicle state variables
    protected Vehicle vehicle;
    protected String vehicleJSON;
    protected DriveState driveState;
    protected GUIState guiState;
    protected VehicleState vehicleState;
    protected ChargeState chargeState;
    protected ClimateState climateState;

    // REST Client API variables
    protected final Client teslaClient = ClientBuilder.newClient();
    protected Client eventClient = ClientBuilder.newClient();
    public final WebTarget teslaTarget = teslaClient.target(URI_OWNERS);
    public final WebTarget tokenTarget = teslaTarget.path(URI_ACCESS_TOKEN);
    public final WebTarget vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
    public final WebTarget vehicleTarget = vehiclesTarget.path(PATH_VEHICLE_ID);
    public final WebTarget dataRequestTarget = vehicleTarget.path(PATH_DATA_REQUEST);
    public final WebTarget commandTarget = vehicleTarget.path(PATH_COMMAND);
    public final WebTarget wakeUpTarget = vehicleTarget.path(PATH_WAKE_UP);
    protected WebTarget eventTarget;

    // Threading and Job related variables
    protected ScheduledFuture<?> connectJob;
    protected Thread eventThread;
    protected ScheduledFuture<?> fastStateJob;
    protected ScheduledFuture<?> slowStateJob;
    protected QueueChannelThrottler stateThrottler;

    protected boolean allowWakeUp = false;
    protected long lastTimeStamp;
    protected long apiIntervalTimestamp;
    protected int apiIntervalErrors;
    protected long eventIntervalTimestamp;
    protected int eventIntervalErrors;
    protected ReentrantLock lock;

    protected double lastLongitude;
    protected double lastLatitude;
    protected long lastLocationChangeTimestamp;

    protected long lastStateTimestamp = System.currentTimeMillis();
    protected String lastState = "";
    protected boolean isInactive = false;

    private StorageService storageService;
    protected Gson gson = new Gson();
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    private TokenResponse logonToken;

    public TeslaHandler(Thing thing, StorageService storageService) {
        super(thing);
        this.storageService = storageService;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Tesla handler for {}", this.getStorageKey());

        updateStatus(ThingStatus.UNKNOWN);

        lock = new ReentrantLock();

        lock.lock();
        try {
            if (connectJob == null || connectJob.isCancelled()) {
                connectJob = scheduler.scheduleWithFixedDelay(connectRunnable, 0, CONNECT_RETRY_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            Map<Object, Rate> channels = new HashMap<>();
            channels.put(DATA_THROTTLE, new Rate(1, 1, TimeUnit.SECONDS));
            channels.put(COMMAND_THROTTLE, new Rate(20, 1, TimeUnit.MINUTES));

            Rate firstRate = new Rate(20, 1, TimeUnit.MINUTES);
            Rate secondRate = new Rate(200, 10, TimeUnit.MINUTES);
            stateThrottler = new QueueChannelThrottler(firstRate, scheduler, channels);
            stateThrottler.addRate(secondRate);

            if (fastStateJob == null || fastStateJob.isCancelled()) {
                fastStateJob = scheduler.scheduleWithFixedDelay(fastStateRunnable, 0, FAST_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            if (slowStateJob == null || slowStateJob.isCancelled()) {
                slowStateJob = scheduler.scheduleWithFixedDelay(slowStateRunnable, 0, SLOW_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        logger.trace("Disposing the Tesla handler for {}", getThing().getUID());

        lock.lock();
        try {
            if (fastStateJob != null && !fastStateJob.isCancelled()) {
                fastStateJob.cancel(true);
                fastStateJob = null;
            }

            if (slowStateJob != null && !slowStateJob.isCancelled()) {
                slowStateJob.cancel(true);
                slowStateJob = null;
            }

            if (eventThread != null && !eventThread.isInterrupted()) {
                eventThread.interrupt();
                eventThread = null;
            }

            if (connectJob != null && !connectJob.isCancelled()) {
                connectJob.cancel(true);
                connectJob = null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

        if (command instanceof RefreshType) {
            if (isAwake()) {
                // Request the state of all known variables. This is sub-optimal, but the requests get scheduled and
                // throttled so we are safe not to break the Tesla SLA
                requestAllData();
            }
        } else {
            if (selector != null) {
                try {
                    switch (selector) {
                        case CHARGE_LIMIT_SOC: {
                            if (command instanceof PercentType) {
                                setChargeLimit(((PercentType) command).intValue());
                            } else if (command instanceof OnOffType && command == OnOffType.ON) {
                                setChargeLimit(100);
                            } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                                setChargeLimit(0);
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.INCREASE) {
                                setChargeLimit(Math.min(chargeState.charge_limit_soc + 1, 100));
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.DECREASE) {
                                setChargeLimit(Math.max(chargeState.charge_limit_soc - 1, 0));
                            }
                            break;
                        }
                        case TEMPERATURE: {
                            if (command instanceof DecimalType) {
                                if (getThing().getProperties().containsKey("temperatureunits")
                                        && getThing().getProperties().get("temperatureunits").equals("F")) {
                                    float fTemp = ((DecimalType) command).floatValue();
                                    float cTemp = ((fTemp - 32.0f) * 5.0f / 9.0f);
                                    setTemperature(cTemp);
                                } else {
                                    setTemperature(((DecimalType) command).floatValue());
                                }
                            }
                            break;
                        }
                        case SUN_ROOF_STATE: {
                            if (command instanceof StringType) {
                                setSunroof(command.toString());
                            }
                            break;
                        }
                        case SUN_ROOF: {
                            if (command instanceof PercentType) {
                                moveSunroof(((PercentType) command).intValue());
                            } else if (command instanceof OnOffType && command == OnOffType.ON) {
                                moveSunroof(100);
                            } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                                moveSunroof(0);
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.INCREASE) {
                                moveSunroof(Math.min(vehicleState.sun_roof_percent_open + 1, 100));
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.DECREASE) {
                                moveSunroof(Math.max(vehicleState.sun_roof_percent_open - 1, 0));
                            }
                            break;
                        }
                        case CHARGE_TO_MAX: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    setMaxRangeCharging(true);
                                } else {
                                    setMaxRangeCharging(false);
                                }
                            }
                            break;
                        }
                        case CHARGE: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    charge(true);
                                } else {
                                    charge(false);
                                }
                            }
                            break;
                        }
                        case FLASH: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    flashLights();
                                }
                            }
                            break;
                        }
                        case HONK_HORN: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    honkHorn();
                                }
                            }
                            break;
                        }
                        case CHARGEPORT: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    openChargePort();
                                }
                            }
                            break;
                        }
                        case DOOR_LOCK: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    lockDoors(true);
                                } else {
                                    lockDoors(false);
                                }
                            }
                            break;
                        }
                        case AUTO_COND: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    autoConditioning(true);
                                } else {
                                    autoConditioning(false);
                                }
                            }
                            break;
                        }
                        case WAKEUP: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    wakeUp();
                                }
                            }
                            break;
                        }
                        case FORCE_REFRESH: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    if (!isOnline()) {
                                        wakeUp();
                                    } else {
                                        setActive();
                                    }
                                }
                            }
                            break;
                        }
                        case ALLOWWAKEUP: {
                            if (command instanceof OnOffType) {
                                allowWakeUp = (((OnOffType) command) == OnOffType.ON);
                            }
                            break;
                        }
                        case ENABLEEVENTS: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    if (eventThread == null) {
                                        eventThread = new Thread(eventRunnable,
                                                "openHAB-Tesla-Events-" + getThing().getUID());
                                        eventThread.start();
                                    }
                                } else {
                                    if (eventThread != null) {
                                        eventThread.interrupt();
                                        eventThread = null;
                                    }
                                }
                            }
                            break;
                        }
                        case FT: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    openFrunk();
                                }
                            }
                            break;
                        }
                        case RT: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    if (vehicleState.rt == 0) {
                                        openTrunk();
                                    }
                                } else {
                                    if (vehicleState.rt == 1) {
                                        closeTrunk();
                                    }
                                }
                            }
                            break;
                        }
                        case VALET_MODE: {
                            if (command instanceof OnOffType) {
                                int valetpin = ((BigDecimal) getConfig().get(VALETPIN)).intValue();
                                if (((OnOffType) command) == OnOffType.ON) {
                                    setValetMode(true, valetpin);
                                } else {
                                    setValetMode(false, valetpin);
                                }
                            }
                            break;
                        }
                        case RESET_VALET_PIN: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    resetValetPin();
                                }
                            }
                            break;
                        }
                        default:
                            break;
                    }
                    return;
                } catch (IllegalArgumentException e) {
                    logger.warn(
                            "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                            channelID, command.toString());
                }
            }
        }
    }

    public void sendCommand(String command, String payLoad, WebTarget target) {
        Request request = new Request(command, payLoad, target);
        if (stateThrottler != null) {
            stateThrottler.submit(COMMAND_THROTTLE, request);
        }
    }

    public void sendCommand(String command) {
        sendCommand(command, "{}");
    }

    public void sendCommand(String command, String payLoad) {
        Request request = new Request(command, payLoad, commandTarget);
        if (stateThrottler != null) {
            stateThrottler.submit(COMMAND_THROTTLE, request);
        }
    }

    public void sendCommand(String command, WebTarget target) {
        Request request = new Request(command, "{}", target);
        if (stateThrottler != null) {
            stateThrottler.submit(COMMAND_THROTTLE, request);
        }
    }

    public void requestData(String command, String payLoad) {
        Request request = new Request(command, payLoad, dataRequestTarget);
        if (stateThrottler != null) {
            stateThrottler.submit(DATA_THROTTLE, request);
        }
    }

    public void requestData(String command) {
        requestData(command, null);
    }

    public void queryVehicle(String parameter) {
        WebTarget target = vehicleTarget.path(parameter);
        sendCommand(parameter, null, target);
    }

    public void requestAllData() {
        requestData(DRIVE_STATE);
        requestData(VEHICLE_STATE);
        requestData(CHARGE_STATE);
        requestData(CLIMATE_STATE);
        requestData(GUI_STATE);
    }

    protected String invokeAndParse(String command, String payLoad, WebTarget target) {
        logger.debug("Invoking: {}", command);

        if (vehicle.id != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                }
            }

            JsonParser parser = new JsonParser();

            if (!checkResponse(response, false)) {
                logger.error("An error occurred while communicating with the vehicle during request {} : {}:{}",
                        new Object[] { command, (response != null) ? response.getStatus() : "",
                                (response != null) ? response.getStatusInfo() : "No Response" });
                return null;
            }

            try {
                JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
                logger.trace("Request : {}:{}:{} yields {}",
                        new Object[] { command, payLoad, target.toString(), jsonObject.get("response").toString() });
                return jsonObject.get("response").toString();
            } catch (Exception e) {
                logger.error("An exception occurred while invoking a REST request : '{}'", e.getMessage());
            }
        }

        return null;
    }

    public void parseAndUpdate(String request, String payLoad, String result) {
        final Double LOCATION_THRESHOLD = .0000001;

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;

        try {
            if (request != null && result != null && !"null".equals(result)) {
                // first, update state objects
                switch (request) {
                    case DRIVE_STATE: {
                        driveState = gson.fromJson(result, DriveState.class);

                        if (Math.abs(lastLatitude - driveState.latitude) > LOCATION_THRESHOLD
                                || Math.abs(lastLongitude - driveState.longitude) > LOCATION_THRESHOLD) {
                            logger.debug("Vehicle moved, resetting last location timestamp");

                            lastLatitude = driveState.latitude;
                            lastLongitude = driveState.longitude;
                            lastLocationChangeTimestamp = System.currentTimeMillis();
                        }

                        break;
                    }
                    case GUI_STATE: {
                        guiState = gson.fromJson(result, GUIState.class);
                        break;
                    }
                    case VEHICLE_STATE: {
                        vehicleState = gson.fromJson(result, VehicleState.class);
                        break;
                    }
                    case CHARGE_STATE: {
                        chargeState = gson.fromJson(result, ChargeState.class);
                        if (isCharging()) {
                            updateState(CHANNEL_CHARGE, OnOffType.ON);
                        } else {
                            updateState(CHANNEL_CHARGE, OnOffType.OFF);
                        }

                        break;
                    }
                    case CLIMATE_STATE: {
                        climateState = gson.fromJson(result, ClimateState.class);
                        break;
                    }
                    case "queryVehicle": {
                        if (vehicle != null && !lastState.equals(vehicle.state)) {
                            lastState = vehicle.state;

                            // in case vehicle changed to online, refresh all data
                            if (isOnline()) {
                                logger.debug("Vehicle is now online, updating all data");
                                lastLocationChangeTimestamp = System.currentTimeMillis();
                                requestAllData();
                            }

                            setActive();
                        }

                        // reset timestamp if elapsed and set inactive to false
                        if (isInactive && lastStateTimestamp + (API_SLEEP_INTERVAL_MINUTES * 60 * 1000) < System
                                .currentTimeMillis()) {
                            logger.debug("Vehicle did not fall asleep within sleep period, checking again");
                            setActive();
                        } else {
                            boolean wasInactive = isInactive;
                            isInactive = !isCharging() && !hasMovedInSleepInterval();

                            if (!wasInactive && isInactive) {
                                lastStateTimestamp = System.currentTimeMillis();
                                logger.debug("Vehicle is inactive");
                            }
                        }

                        break;
                    }
                }

                // secondly, reformat the response string to a JSON compliant
                // object for some specific non-JSON compatible requests
                switch (request) {
                    case MOBILE_ENABLED_STATE: {
                        jsonObject = new JsonObject();
                        jsonObject.addProperty(MOBILE_ENABLED_STATE, result);
                        break;
                    }
                    default: {
                        jsonObject = parser.parse(result).getAsJsonObject();
                        break;
                    }
                }
            }

            // process the result
            if (jsonObject != null && result != null && !"null".equals(result)) {
                // deal with responses for "set" commands, which get confirmed
                // positively, or negatively, in which case a reason for failure
                // is provided
                if (jsonObject.get("reason") != null && jsonObject.get("reason").getAsString() != null) {
                    boolean requestResult = jsonObject.get("result").getAsBoolean();
                    logger.debug("The request ({}) execution was {}, and reported '{}'", new Object[] { request,
                            requestResult ? "successful" : "not successful", jsonObject.get("reason").getAsString() });
                } else {
                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

                    long resultTimeStamp = 0;
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        if ("timestamp".equals(entry.getKey())) {
                            resultTimeStamp = Long.valueOf(entry.getValue().getAsString());
                            if (logger.isTraceEnabled()) {
                                Date date = new Date(resultTimeStamp);
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                logger.trace("The request result timestamp is {}", dateFormatter.format(date));
                            }
                            break;
                        }
                    }

                    try {
                        lock.lock();

                        boolean proceed = true;
                        if (resultTimeStamp < lastTimeStamp && request == DRIVE_STATE) {
                            proceed = false;
                        }

                        if (proceed) {
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                try {
                                    TeslaChannelSelector selector = TeslaChannelSelector
                                            .getValueSelectorFromRESTID(entry.getKey());
                                    if (!selector.isProperty()) {
                                        if (!entry.getValue().isJsonNull()) {
                                            updateState(selector.getChannelID(), teslaChannelSelectorProxy.getState(
                                                    entry.getValue().getAsString(), selector, editProperties()));
                                            if (logger.isTraceEnabled()) {
                                                logger.trace(
                                                        "The variable/value pair '{}':'{}' is successfully processed",
                                                        entry.getKey(), entry.getValue());
                                            }
                                        } else {
                                            updateState(selector.getChannelID(), UnDefType.UNDEF);
                                        }
                                    } else {
                                        if (!entry.getValue().isJsonNull()) {
                                            Map<String, String> properties = editProperties();
                                            properties.put(selector.getChannelID(), entry.getValue().getAsString());
                                            updateProperties(properties);
                                            if (logger.isTraceEnabled()) {
                                                logger.trace(
                                                        "The variable/value pair '{}':'{}' is successfully used to set property '{}'",
                                                        entry.getKey(), entry.getValue(), selector.getChannelID());
                                            }
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    logger.trace("The variable/value pair '{}':'{}' is not (yet) supported",
                                            entry.getKey(), entry.getValue());
                                } catch (ClassCastException | IllegalStateException e) {
                                    logger.trace("An exception occurred while converting the JSON data : '{}'",
                                            e.getMessage(), e);
                                }
                            }
                        } else {
                            logger.warn("The result for request '{}' is discarded due to an out of sync timestamp",
                                    request);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (Exception p) {
            logger.error("An exception occurred while parsing data received from the vehicle: '{}'", p.getMessage());
        }
    }

    protected boolean isAwake() {
        return vehicle != null && "online".equals(vehicle.state) && vehicle.vehicle_id != null;
    }

    protected boolean isOnline() {
        return vehicle != null && "online".equals(vehicle.state) && vehicle.vehicle_id != null;
    }

    protected boolean isInMotion() {
        if (driveState != null) {
            if (driveState.speed != null && driveState.shift_state != null) {
                return !"Undefined".equals(driveState.speed)
                        && (!"P".equals(driveState.shift_state) || !"Undefined".equals(driveState.shift_state));
            }
        }
        return false;
    }

    protected boolean isInactive() {
        // vehicle is inactive in case
        // - it does not charge
        // - it has not moved in the observation period
        return isInactive && !isCharging() && !hasMovedInSleepInterval();
    }

    protected boolean isCharging() {
        return chargeState != null && "Charging".equals(chargeState.charging_state);
    }

    protected boolean hasMovedInSleepInterval() {
        return lastLocationChangeTimestamp > (System.currentTimeMillis()
                - (MOVE_THRESHOLD_INTERVAL_MINUTES * 60 * 1000));
    }

    protected boolean allowQuery() {
        return allowWakeUp || (isOnline() && !isInactive());
    }

    protected void setActive() {
        isInactive = false;
        lastLocationChangeTimestamp = System.currentTimeMillis();
        lastLatitude = 0;
        lastLongitude = 0;
    }

    protected boolean checkResponse(Response response, boolean immediatelyFail) {

        if (response != null && response.getStatus() == 200) {
            return true;
        } else {
            apiIntervalErrors++;
            if (immediatelyFail || apiIntervalErrors >= API_MAXIMUM_ERRORS_IN_INTERVAL) {
                if (immediatelyFail) {
                    logger.warn("Got an unsuccessful result, setting vehicle to offline and will try again");
                } else {
                    logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                            API_MAXIMUM_ERRORS_IN_INTERVAL, API_ERROR_INTERVAL_SECONDS);
                }

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                eventClient.close();
            } else if ((System.currentTimeMillis() - apiIntervalTimestamp) > 1000 * API_ERROR_INTERVAL_SECONDS) {
                logger.trace("Resetting the error counter. ({} errors in the last interval)", apiIntervalErrors);
                apiIntervalTimestamp = System.currentTimeMillis();
                apiIntervalErrors = 0;
            }
        }

        return false;
    }

    public void setChargeLimit(int percent) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("percent", percent);
        sendCommand(COMMAND_SET_CHARGE_LIMIT, gson.toJson(payloadObject), commandTarget);
        requestData(CHARGE_STATE);
    }

    public void setSunroof(String state) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("state", state);
        sendCommand(COMMAND_SUN_ROOF, gson.toJson(payloadObject), commandTarget);
        requestData(VEHICLE_STATE);
    }

    public void moveSunroof(int percent) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("state", "move");
        payloadObject.addProperty("percent", percent);
        sendCommand(COMMAND_SUN_ROOF, gson.toJson(payloadObject), commandTarget);
        requestData(VEHICLE_STATE);
    }

    public void setTemperature(float temperature) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("driver_temp", temperature);
        payloadObject.addProperty("passenger_temp", temperature);
        sendCommand(COMMAND_SET_TEMP, gson.toJson(payloadObject), commandTarget);
        requestData(CLIMATE_STATE);
    }

    public void openFrunk() {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("which_trunk", "front");
        sendCommand(COMMAND_TRUNK_OPEN, gson.toJson(payloadObject), commandTarget);
        requestData(VEHICLE_STATE);
    }

    public void openTrunk() {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("which_trunk", "rear");
        sendCommand(COMMAND_TRUNK_OPEN, gson.toJson(payloadObject), commandTarget);
        requestData(VEHICLE_STATE);
    }

    public void closeTrunk() {
        openTrunk();
    }

    public void setValetMode(boolean b, Integer pin) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("on", b);
        if (pin != null) {
            payloadObject.addProperty("password", String.format("%04d", pin));
        }
        sendCommand(COMMAND_SET_VALET_MODE, gson.toJson(payloadObject), commandTarget);
        requestData(CLIMATE_STATE);
    }

    public void resetValetPin() {
        sendCommand(COMMAND_RESET_VALET_PIN, commandTarget);
        requestData(CLIMATE_STATE);
    }

    public void setMaxRangeCharging(boolean b) {
        if (b) {
            sendCommand(COMMAND_CHARGE_MAX, commandTarget);
        } else {
            sendCommand(COMMAND_CHARGE_STD, commandTarget);
        }
        requestData(CHARGE_STATE);
    }

    public void charge(boolean b) {
        if (b) {
            sendCommand(COMMAND_CHARGE_START, commandTarget);
        } else {
            sendCommand(COMMAND_CHARGE_STOP, commandTarget);
        }
        requestData(CHARGE_STATE);
    }

    public void flashLights() {
        sendCommand(COMMAND_FLASH_LIGHTS, commandTarget);
    }

    public void honkHorn() {
        sendCommand(COMMAND_HONK_HORN, commandTarget);
    }

    public void openChargePort() {
        sendCommand(COMMAND_OPEN_CHARGE_PORT, commandTarget);
        requestData(CHARGE_STATE);
    }

    public void lockDoors(boolean b) {
        if (b) {
            sendCommand(COMMAND_DOOR_LOCK, commandTarget);
        } else {
            sendCommand(COMMAND_DOOR_UNLOCK, commandTarget);
        }
        requestData(VEHICLE_STATE);
    }

    public void autoConditioning(boolean b) {
        if (b) {
            sendCommand(COMMAND_AUTO_COND_START, commandTarget);
        } else {
            sendCommand(COMMAND_AUTO_COND_STOP, commandTarget);
        }
        requestData(CLIMATE_STATE);
    }

    public void wakeUp() {
        sendCommand(COMMAND_WAKE_UP, wakeUpTarget);
    }

    protected Vehicle queryVehicle() {
        // get a list of vehicles
        Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + logonToken.access_token).get();

        logger.debug("Querying the vehicle : Response : {}:{}", response.getStatus(), response.getStatusInfo());

        if (!checkResponse(response, true)) {
            logger.error("An error occurred while querying the vehicle");
            return null;
        }

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

        for (int i = 0; i < vehicleArray.length; i++) {
            logger.debug("Querying the vehicle : VIN : {}", vehicleArray[i].vin);
            if (vehicleArray[i].vin.equals(getConfig().get(VIN))) {
                vehicleJSON = gson.toJson(vehicleArray[i]);
                parseAndUpdate("queryVehicle", null, vehicleJSON);
                if (logger.isTraceEnabled()) {
                    logger.trace("Vehicle is id {}/vehicle_id {}/tokens {}", vehicleArray[i].id,
                            vehicleArray[i].vehicle_id, vehicleArray[i].tokens);
                }
                return vehicleArray[i];
            }
        }

        return null;
    }

    protected void queryVehicleAndUpdate() {
        vehicle = queryVehicle();
        parseAndUpdate("queryVehicle", null, vehicleJSON);
    }

    private String getStorageKey() {
        return this.getThing().getUID().getId();
    }

    private ThingStatusDetail authenticate() {
        Storage<Object> storage = storageService.getStorage(TeslaBindingConstants.BINDING_ID);

        String storedToken = (String) storage.get(getStorageKey());
        TokenResponse token = storedToken == null ? null : gson.fromJson(storedToken, TokenResponse.class);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        boolean hasExpired = true;

        if (token != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(token.created_at * 1000);
            logger.info("Found a request token created at {}", dateFormatter.format(calendar.getTime()));
            calendar.setTimeInMillis(token.created_at * 1000 + 60 * token.expires_in);

            Date now = new Date();

            if (calendar.getTime().before(now)) {
                logger.info("The token has expired at {}", dateFormatter.format(calendar.getTime()));
                hasExpired = true;
            } else {
                hasExpired = false;
            }
        }

        String username = (String) getConfig().get(USERNAME);

        if (!StringUtils.isEmpty(username) && hasExpired) {
            String password = (String) getConfig().get(PASSWORD);
            return authenticate(username, password);
        }

        if (token == null || StringUtils.isEmpty(token.refresh_token)) {
            return ThingStatusDetail.CONFIGURATION_ERROR;
        }

        TokenRequestRefreshToken tokenRequest = null;
        try {
            tokenRequest = new TokenRequestRefreshToken(token.refresh_token);
        } catch (GeneralSecurityException e) {
            logger.error("An exception occurred while requesting a new token : '{}'", e.getMessage(), e);
        }

        String payLoad = gson.toJson(tokenRequest);

        Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

        if (response == null) {
            logger.debug("Authenticating : Response was null");
        } else {
            logger.debug("Authenticating : Response : {}:{}", response.getStatus(), response.getStatusInfo());

            if (response.getStatus() == 200 && response.hasEntity()) {
                String responsePayLoad = response.readEntity(String.class);
                TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                if (tokenResponse != null && !StringUtils.isEmpty(tokenResponse.access_token)) {
                    storage.put(getStorageKey(), gson.toJson(tokenResponse));
                    this.logonToken = tokenResponse;
                    if (logger.isTraceEnabled()) {
                        logger.trace("Access Token is {}", logonToken.access_token);
                    }
                    return ThingStatusDetail.NONE;
                }

                return ThingStatusDetail.NONE;
            } else if (response.getStatus() == 401) {
                if (!StringUtils.isEmpty(username)) {
                    String password = (String) getConfig().get(PASSWORD);
                    return authenticate(username, password);
                } else {
                    return ThingStatusDetail.CONFIGURATION_ERROR;
                }
            } else if (response.getStatus() == 503 || response.getStatus() == 502) {
                return ThingStatusDetail.COMMUNICATION_ERROR;
            }
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }

    private ThingStatusDetail authenticate(String username, String password) {
        TokenRequest token = null;
        try {
            token = new TokenRequestPassword(username, password);
        } catch (GeneralSecurityException e) {
            logger.error("An exception occurred while building a password request token : '{}'", e.getMessage(), e);
        }

        if (token != null) {
            String payLoad = gson.toJson(token);

            Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

            if (response != null) {
                logger.debug("Authenticating : Response : {}:{}", response.getStatus(), response.getStatusInfo());

                if (response.getStatus() == 200 && response.hasEntity()) {
                    String responsePayLoad = response.readEntity(String.class);
                    TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                    if (StringUtils.isNotEmpty(tokenResponse.access_token)) {
                        Storage<Object> storage = storageService.getStorage(TeslaBindingConstants.BINDING_ID);
                        storage.put(getStorageKey(), gson.toJson(tokenResponse));
                        this.logonToken = tokenResponse;
                        return ThingStatusDetail.NONE;
                    }
                } else if (response.getStatus() == 401) {
                    return ThingStatusDetail.CONFIGURATION_ERROR;
                } else if (response.getStatus() == 503 || response.getStatus() == 502) {
                    return ThingStatusDetail.COMMUNICATION_ERROR;
                }
            }
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }

    protected Runnable fastStateRunnable = () -> {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            boolean allowQuery = allowQuery();

            if (allowQuery) {
                requestData(DRIVE_STATE);
                requestData(VEHICLE_STATE);
            } else {
                if (vehicle == null) {
                    vehicle = queryVehicle();
                } else if (allowWakeUp) {
                    wakeUp();
                } else {
                    queryVehicleAndUpdate();

                    if (isOnline()) {
                        logger.debug("Vehicle is neither charging nor moving, skipping updates to allow it to sleep");
                    }
                }
            }
        }

        if (allowWakeUp) {
            updateState(CHANNEL_ALLOWWAKEUP, OnOffType.ON);
        } else {
            updateState(CHANNEL_ALLOWWAKEUP, OnOffType.OFF);
        }

        if (eventThread != null) {
            updateState(CHANNEL_ENABLEEVENTS, OnOffType.ON);
        } else {
            updateState(CHANNEL_ENABLEEVENTS, OnOffType.OFF);
        }
    };

    protected Runnable slowStateRunnable = () -> {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            boolean allowQuery = allowQuery();

            if (allowQuery) {
                requestData(CHARGE_STATE);
                requestData(CLIMATE_STATE);
                requestData(GUI_STATE);
                queryVehicle(MOBILE_ENABLED_STATE);
                parseAndUpdate("queryVehicle", null, vehicleJSON);
            } else {
                if (vehicle == null) {
                    vehicle = queryVehicle();
                } else if (allowWakeUp) {
                    wakeUp();
                } else {
                    queryVehicleAndUpdate();

                    if (isOnline()) {
                        logger.debug("Vehicle is neither charging nor moving, skipping updates to allow it to sleep");
                    }
                }
            }
        }
    };

    protected Runnable connectRunnable = () -> {
        try {
            lock.lock();

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                logger.debug("Setting up an authenticated connection to the Tesla back-end");

                ThingStatusDetail authenticationResult = authenticate();

                if (authenticationResult != ThingStatusDetail.NONE) {
                    updateStatus(ThingStatus.OFFLINE, authenticationResult);
                } else {
                    // get a list of vehicles
                    Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();

                    if (response != null && response.getStatus() == 200 && response.hasEntity()) {
                        if ((vehicle = queryVehicle()) != null) {
                            logger.debug("Found the vehicle with VIN '{}' in the list of vehicles you own",
                                    getConfig().get(VIN));
                            updateStatus(ThingStatus.ONLINE);
                            apiIntervalErrors = 0;
                            apiIntervalTimestamp = System.currentTimeMillis();
                        } else {
                            logger.warn("Unable to find the vehicle with VIN '{}' in the list of vehicles you own",
                                    getConfig().get(VIN));
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    } else {
                        if (response != null) {
                            logger.error("Error fetching the list of vehicles : {}:{}", response.getStatus(),
                                    response.getStatusInfo());
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while connecting to the Tesla back-end: '{}'", e.getMessage());
        } finally {
            lock.unlock();
        }
    };

    protected Runnable eventRunnable = new Runnable() {
        Response eventResponse;
        BufferedReader eventBufferedReader;
        InputStreamReader eventInputStreamReader;
        boolean isEstablished = false;

        protected boolean establishEventStream() {
            try {
                if (!isEstablished) {
                    eventBufferedReader = null;

                    eventClient = ClientBuilder.newClient()
                            .property(ClientProperties.CONNECT_TIMEOUT, EVENT_STREAM_CONNECT_TIMEOUT)
                            .property(ClientProperties.READ_TIMEOUT, EVENT_STREAM_READ_TIMEOUT)
                            .register(new Authenticator((String) getConfig().get(USERNAME), vehicle.tokens[0]));
                    eventTarget = eventClient.target(URI_EVENT).path(vehicle.vehicle_id + "/").queryParam("values",
                            StringUtils.join(EventKeys.values(), ',', 1, EventKeys.values().length));
                    eventResponse = eventTarget.request(MediaType.TEXT_PLAIN_TYPE).get();

                    logger.debug("Event Stream : Establishing the event stream : Response : {}:{}",
                            eventResponse.getStatus(), eventResponse.getStatusInfo());

                    if (eventResponse.getStatus() == 200) {
                        InputStream dummy = (InputStream) eventResponse.getEntity();
                        eventInputStreamReader = new InputStreamReader(dummy);
                        eventBufferedReader = new BufferedReader(eventInputStreamReader);
                        isEstablished = true;
                    } else if (eventResponse.getStatus() == 401) {
                        isEstablished = false;
                    } else {
                        isEstablished = false;
                    }

                    if (!isEstablished) {
                        eventIntervalErrors++;
                        if (eventIntervalErrors >= EVENT_MAXIMUM_ERRORS_IN_INTERVAL) {
                            logger.warn(
                                    "Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                                    EVENT_MAXIMUM_ERRORS_IN_INTERVAL, EVENT_ERROR_INTERVAL_SECONDS);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                            eventClient.close();
                        }

                        if ((System.currentTimeMillis() - eventIntervalTimestamp) > 1000
                                * EVENT_ERROR_INTERVAL_SECONDS) {
                            logger.trace("Resetting the error counter. ({} errors in the last interval)",
                                    eventIntervalErrors);
                            eventIntervalTimestamp = System.currentTimeMillis();
                            eventIntervalErrors = 0;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(
                        "Event Stream : An exception occurred while establishing the event stream for the vehicle: '{}'",
                        e.getMessage());
                isEstablished = false;
            }

            return isEstablished;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        if (isAwake()) {
                            if (establishEventStream()) {
                                String line = eventBufferedReader.readLine();

                                while (line != null) {
                                    logger.debug("Event Stream : Received an event: '{}'", line);
                                    String vals[] = line.split(",");
                                    long currentTimeStamp = Long.valueOf(vals[0]);
                                    long systemTimeStamp = System.currentTimeMillis();
                                    if (logger.isDebugEnabled()) {
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                        logger.debug("STS {} CTS {} Delta {}",
                                                dateFormatter.format(new Date(systemTimeStamp)),
                                                dateFormatter.format(new Date(currentTimeStamp)),
                                                systemTimeStamp - currentTimeStamp);
                                    }
                                    if (systemTimeStamp - currentTimeStamp < EVENT_TIMESTAMP_AGE_LIMIT) {
                                        if (currentTimeStamp > lastTimeStamp) {
                                            lastTimeStamp = Long.valueOf(vals[0]);
                                            if (logger.isDebugEnabled()) {
                                                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                logger.debug("Event Stream : Event stamp is {}",
                                                        dateFormatter.format(new Date(lastTimeStamp)));
                                            }
                                            for (int i = 0; i < EventKeys.values().length; i++) {
                                                TeslaChannelSelector selector = TeslaChannelSelector
                                                        .getValueSelectorFromRESTID((EventKeys.values()[i]).toString());
                                                if (!selector.isProperty()) {
                                                    State newState = teslaChannelSelectorProxy.getState(vals[i],
                                                            selector, editProperties());
                                                    if (newState != null && !"".equals(vals[i])) {
                                                        updateState(selector.getChannelID(), newState);
                                                    } else {
                                                        updateState(selector.getChannelID(), UnDefType.UNDEF);
                                                    }
                                                } else {
                                                    Map<String, String> properties = editProperties();
                                                    properties.put(selector.getChannelID(),
                                                            (selector.getState(vals[i])).toString());
                                                    updateProperties(properties);
                                                }
                                            }
                                        } else {
                                            if (logger.isDebugEnabled()) {
                                                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                logger.debug(
                                                        "Event Stream : Discarding an event with an out of sync timestamp {} (last is {})",
                                                        dateFormatter.format(new Date(currentTimeStamp)),
                                                        dateFormatter.format(new Date(lastTimeStamp)));
                                            }
                                        }
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                            logger.debug(
                                                    "Event Stream : Discarding an event that differs {} ms from the system time: {} (system is {})",
                                                    systemTimeStamp - currentTimeStamp,
                                                    dateFormatter.format(currentTimeStamp),
                                                    dateFormatter.format(systemTimeStamp));
                                        }
                                        if (systemTimeStamp - currentTimeStamp > EVENT_TIMESTAMP_MAX_DELTA) {
                                            if (logger.isTraceEnabled()) {
                                                logger.trace("Event Stream : The event stream will be reset");
                                            }
                                            isEstablished = false;
                                        }
                                    }

                                    line = eventBufferedReader.readLine();
                                }

                                if (line == null) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Event Stream : The end of stream was reached");
                                    }
                                    isEstablished = false;
                                }
                            }
                        } else {
                            logger.debug("Event stream : The vehicle is not awake");
                            if (vehicle != null && allowWakeUp) {
                                // wake up the vehicle until streaming token <> 0
                                logger.debug("Event stream : Waking up the vehicle");
                                wakeUp();
                            } else {
                                logger.debug("Event stream : Querying the vehicle");
                                vehicle = queryVehicle();
                            }
                        }
                    } else {
                        Thread.sleep(250);
                    }
                } catch (IOException | NumberFormatException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Event Stream : An exception occurred while reading events : '{}'",
                                e.getMessage());
                    }
                    isEstablished = false;
                } catch (InterruptedException e) {
                    isEstablished = false;
                }

                if (Thread.interrupted()) {
                    logger.debug("Event Stream : the Event Stream was interrupted");
                    return;
                }
            }
        }
    };

    protected class Request implements Runnable {

        private String request;
        private String payLoad;
        private WebTarget target;

        public Request(String request, String payLoad, WebTarget target) {
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
        }

        @Override
        public void run() {
            try {
                String result = "";

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    if (request.equals(COMMAND_WAKE_UP) || isAwake()) {
                        result = invokeAndParse(request, payLoad, target);
                    }
                }

                if (result != null && !"".equals(result)) {
                    parseAndUpdate(request, payLoad, result);
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the vehicle: '{}'", e.getMessage());
            }
        }
    }

    protected class Authenticator implements ClientRequestFilter {
        private final String user;
        private final String password;

        public Authenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);
        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        }
    }
}
