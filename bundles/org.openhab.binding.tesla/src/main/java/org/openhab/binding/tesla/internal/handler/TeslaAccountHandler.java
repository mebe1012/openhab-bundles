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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.discovery.TeslaVehicleDiscoveryService;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleConfig;
import org.openhab.binding.tesla.internal.protocol.sso.TokenResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaAccountHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 * @author Kai Kreuzer - refactored to use separate vehicle handlers
 */
public class TeslaAccountHandler extends BaseBridgeHandler {

    public static final int API_MAXIMUM_ERRORS_IN_INTERVAL = 2;
    public static final int API_ERROR_INTERVAL_SECONDS = 15;
    private static final int CONNECT_RETRY_INTERVAL = 15000;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Logger logger = LoggerFactory.getLogger(TeslaAccountHandler.class);

    // REST Client API variables
    private final WebTarget teslaTarget;
    WebTarget vehiclesTarget; // this cannot be marked final as it is used in the runnable
    final WebTarget vehicleTarget;
    final WebTarget dataRequestTarget;
    final WebTarget commandTarget;
    final WebTarget wakeUpTarget;

    private final TeslaSSOHandler ssoHandler;

    // Threading and Job related variables
    protected ScheduledFuture<?> connectJob;

    protected long lastTimeStamp;
    protected long apiIntervalTimestamp;
    protected int apiIntervalErrors;
    protected long eventIntervalTimestamp;
    protected int eventIntervalErrors;
    protected ReentrantLock lock;

    private final Gson gson = new Gson();

    private TokenResponse logonToken;
    private final Set<VehicleListener> vehicleListeners = new HashSet<>();

    public TeslaAccountHandler(Bridge bridge, Client teslaClient, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.teslaTarget = teslaClient.target(URI_OWNERS);
        this.ssoHandler = new TeslaSSOHandler(httpClientFactory.getCommonHttpClient());

        this.vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
        this.vehicleTarget = vehiclesTarget.path(PATH_VEHICLE_ID);
        this.dataRequestTarget = vehicleTarget.path(PATH_DATA_REQUEST);
        this.commandTarget = vehicleTarget.path(PATH_COMMAND);
        this.wakeUpTarget = vehicleTarget.path(PATH_WAKE_UP);
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Tesla account handler for {}", this.getStorageKey());

        updateStatus(ThingStatus.UNKNOWN);

        lock = new ReentrantLock();
        lock.lock();

        try {
            if (connectJob == null || connectJob.isCancelled()) {
                connectJob = scheduler.scheduleWithFixedDelay(connectRunnable, 0, CONNECT_RETRY_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        logger.trace("Disposing the Tesla account handler for {}", getThing().getUID());

        lock.lock();
        try {
            if (connectJob != null && !connectJob.isCancelled()) {
                connectJob.cancel(true);
                connectJob = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void scanForVehicles() {
        scheduler.execute(() -> queryVehicles());
    }

    public void addVehicleListener(VehicleListener listener) {
        this.vehicleListeners.add(listener);
    }

    public void removeVehicleListener(VehicleListener listener) {
        this.vehicleListeners.remove(listener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    public String getAuthHeader() {
        if (logonToken != null) {
            return "Bearer " + logonToken.access_token;
        } else {
            return null;
        }
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
            } else if ((System.currentTimeMillis() - apiIntervalTimestamp) > 1000 * API_ERROR_INTERVAL_SECONDS) {
                logger.trace("Resetting the error counter. ({} errors in the last interval)", apiIntervalErrors);
                apiIntervalTimestamp = System.currentTimeMillis();
                apiIntervalErrors = 0;
            }
        }

        return false;
    }

    protected Vehicle[] queryVehicles() {
        String authHeader = getAuthHeader();

        if (authHeader != null) {
            // get a list of vehicles
            Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", authHeader).get();

            logger.debug("Querying the vehicle: Response: {}:{}", response.getStatus(), response.getStatusInfo());

            if (!checkResponse(response, true)) {
                logger.error("An error occurred while querying the vehicle");
                return null;
            }

            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();
            Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

            for (Vehicle vehicle : vehicleArray) {
                String responseString = invokeAndParse(vehicle.id, VEHICLE_CONFIG, null, dataRequestTarget);
                if (responseString == null || responseString.isBlank()) {
                    continue;
                }
                VehicleConfig vehicleConfig = gson.fromJson(responseString, VehicleConfig.class);
                for (VehicleListener listener : vehicleListeners) {
                    listener.vehicleFound(vehicle, vehicleConfig);
                }
                for (Thing vehicleThing : getThing().getThings()) {
                    if (vehicle.vin.equals(vehicleThing.getConfiguration().get(VIN))) {
                        TeslaVehicleHandler vehicleHandler = (TeslaVehicleHandler) vehicleThing.getHandler();
                        if (vehicleHandler != null) {
                            logger.debug("Querying the vehicle: VIN {}", vehicle.vin);
                            String vehicleJSON = gson.toJson(vehicle);
                            vehicleHandler.parseAndUpdate("queryVehicle", null, vehicleJSON);
                            logger.trace("Vehicle is id {}/vehicle_id {}/tokens {}", vehicle.id, vehicle.vehicle_id,
                                    vehicle.tokens);
                        }
                    }
                }
            }
            return vehicleArray;
        } else {
            return new Vehicle[0];
        }
    }

    private String getStorageKey() {
        return this.getThing().getUID().getId();
    }

    private ThingStatusInfo authenticate() {
        TokenResponse token = logonToken;

        boolean hasExpired = true;

        if (token != null) {
            Instant tokenCreationInstant = Instant.ofEpochMilli(token.created_at * 1000);
            logger.debug("Found a request token created at {}", dateFormatter.format(tokenCreationInstant));
            Instant tokenExpiresInstant = Instant.ofEpochMilli(token.created_at * 1000 + 60 * token.expires_in);

            if (tokenExpiresInstant.isBefore(Instant.now())) {
                logger.debug("The token has expired at {}", dateFormatter.format(tokenExpiresInstant));
                hasExpired = true;
            } else {
                hasExpired = false;
            }
        }

        if (hasExpired) {
            String username = (String) getConfig().get(CONFIG_USERNAME);
            String password = (String) getConfig().get(CONFIG_PASSWORD);
            String refreshToken = (String) getConfig().get(CONFIG_REFRESHTOKEN);

            if (refreshToken == null || refreshToken.isEmpty()) {
                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                    try {
                        refreshToken = ssoHandler.authenticate(username, password);
                    } catch (Exception e) {
                        logger.error("An exception occurred while obtaining refresh token with username/password: '{}'",
                                e.getMessage());
                    }

                    if (refreshToken != null) {
                        // store refresh token from SSO endpoint in config, clear the password
                        Configuration cfg = editConfiguration();
                        cfg.put(TeslaBindingConstants.CONFIG_REFRESHTOKEN, refreshToken);
                        cfg.remove(TeslaBindingConstants.CONFIG_PASSWORD);
                        updateConfiguration(cfg);
                    } else {
                        return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Failed to obtain refresh token with username/password.");
                    }
                } else {
                    return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Neither a refresh token nor credentials are provided.");
                }
            }

            this.logonToken = ssoHandler.getAccessToken(refreshToken);
            if (this.logonToken == null) {
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Failed to obtain access token for API.");
            }
        }

        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    protected String invokeAndParse(String vehicleId, String command, String payLoad, WebTarget target) {
        logger.debug("Invoking: {}", command);

        if (vehicleId != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                } else {
                    response = target.resolveTemplate("vid", vehicleId).request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                }
            }

            if (!checkResponse(response, false)) {
                logger.debug("An error occurred while communicating with the vehicle during request {}: {}:{}", command,
                        (response != null) ? response.getStatus() : "",
                        (response != null) ? response.getStatusInfo() : "No Response");
                return null;
            }

            try {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();
                logger.trace("Request : {}:{}:{} yields {}", command, payLoad, target, jsonObject.get("response"));
                return jsonObject.get("response").toString();
            } catch (Exception e) {
                logger.error("An exception occurred while invoking a REST request: '{}'", e.getMessage());
            }
        }

        return null;
    }

    protected Runnable connectRunnable = () -> {
        try {
            lock.lock();

            ThingStatusInfo status = getThing().getStatusInfo();
            if (status.getStatus() != ThingStatus.ONLINE
                    && status.getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
                logger.debug("Setting up an authenticated connection to the Tesla back-end");

                ThingStatusInfo authenticationResult = authenticate();
                updateStatus(authenticationResult.getStatus(), authenticationResult.getStatusDetail(),
                        authenticationResult.getDescription());

                if (authenticationResult.getStatus() == ThingStatus.ONLINE) {
                    // get a list of vehicles
                    Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();

                    if (response != null && response.getStatus() == 200 && response.hasEntity()) {
                        updateStatus(ThingStatus.ONLINE);
                        for (Vehicle vehicle : queryVehicles()) {
                            Bridge bridge = getBridge();
                            if (bridge != null) {
                                List<Thing> things = bridge.getThings();
                                for (int i = 0; i < things.size(); i++) {
                                    Thing thing = things.get(i);
                                    TeslaVehicleHandler handler = (TeslaVehicleHandler) thing.getHandler();
                                    if (handler != null) {
                                        if (vehicle.vin.equals(thing.getConfiguration().get(VIN))) {
                                            logger.debug(
                                                    "Found the vehicle with VIN '{}' in the list of vehicles you own",
                                                    getConfig().get(VIN));
                                            apiIntervalErrors = 0;
                                            apiIntervalTimestamp = System.currentTimeMillis();
                                        } else {
                                            logger.warn(
                                                    "Unable to find the vehicle with VIN '{}' in the list of vehicles you own",
                                                    getConfig().get(VIN));
                                            handler.updateStatus(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.CONFIGURATION_ERROR,
                                                    "Vin is not available through this account.");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (response != null) {
                            logger.error("Error fetching the list of vehicles : {}:{}", response.getStatus(),
                                    response.getStatusInfo());
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                } else if (authenticationResult.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
                    // make sure to set thing to CONFIGURATION_ERROR in case of failed authentication in order not to
                    // hit request limit on retries on the Tesla SSO endpoints.
                    updateStatus(ThingStatus.OFFLINE, authenticationResult.getStatusDetail());
                }

            }
        } catch (Exception e) {
            logger.error("An exception occurred while connecting to the Tesla back-end: '{}'", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    };

    public static class Authenticator implements ClientRequestFilter {
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

    protected class Request implements Runnable {

        private TeslaVehicleHandler handler;
        private String request;
        private String payLoad;
        private WebTarget target;

        public Request(TeslaVehicleHandler handler, String request, String payLoad, WebTarget target) {
            this.handler = handler;
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
        }

        @Override
        public void run() {
            try {
                String result = "";

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    result = invokeAndParse(handler.getVehicleId(), request, payLoad, target);
                    if (result != null && !"".equals(result)) {
                        handler.parseAndUpdate(request, payLoad, result);
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the vehicle: '{}'", e.getMessage(), e);
            }
        }
    }

    public Request newRequest(TeslaVehicleHandler teslaVehicleHandler, String command, String payLoad,
            WebTarget target) {
        return new Request(teslaVehicleHandler, command, payLoad, target);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(TeslaVehicleDiscoveryService.class);
    }
}
