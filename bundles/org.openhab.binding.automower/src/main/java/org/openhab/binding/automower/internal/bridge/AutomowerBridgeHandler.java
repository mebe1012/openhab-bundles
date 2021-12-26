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
package org.openhab.binding.automower.internal.bridge;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AutomowerBridgeHandler.class);

    private static final String HUSQVARNA_API_TOKEN_URL = "https://api.authentication.husqvarnagroup.dev/v1/oauth2/token";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final long DEFAULT_POLLING_INTERVAL_S = TimeUnit.HOURS.toSeconds(1);

    private final OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private @Nullable ScheduledFuture<?> automowerBridgePollingJob;
    private @Nullable AutomowerBridge bridge;
    private final HttpClient httpClient;

    public AutomowerBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    private void pollAutomowers(AutomowerBridge bridge) {
        MowerListResult automowers;
        try {
            automowers = bridge.getAutomowers();
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Found {} automowers", automowers.getData().size());
        } catch (AutomowerCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-mowers-failed");
            logger.warn("Unable to fetch automowers: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        AutomowerBridge currentBridge = bridge;
        if (currentBridge != null) {
            stopAutomowerBridgePolling(currentBridge);
            bridge = null;
        }
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
    }

    @Override
    public void initialize() {
        AutomowerBridgeConfiguration bridgeConfiguration = getConfigAs(AutomowerBridgeConfiguration.class);

        final String appKey = bridgeConfiguration.getAppKey();
        final String userName = bridgeConfiguration.getUserName();
        final String password = bridgeConfiguration.getPassword();
        final Integer pollingIntervalS = bridgeConfiguration.getPollingInterval();

        if (appKey == null || appKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-app-key");
        } else if (userName == null || userName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-username");
        } else if (password == null || password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-password");
        } else if (pollingIntervalS != null && pollingIntervalS < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-invalid-polling-interval");
        } else {
            oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), HUSQVARNA_API_TOKEN_URL,
                    null, appKey, null, null, null);

            if (bridge == null) {
                AutomowerBridge currentBridge = new AutomowerBridge(oAuthService, appKey, userName, password,
                        httpClient, scheduler);
                bridge = currentBridge;
                startAutomowerBridgePolling(currentBridge, pollingIntervalS);
            }
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    private void startAutomowerBridgePolling(AutomowerBridge bridge, @Nullable Integer pollingIntervalS) {
        ScheduledFuture<?> currentPollingJob = automowerBridgePollingJob;
        if (currentPollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalS == null ? DEFAULT_POLLING_INTERVAL_S : pollingIntervalS;
            automowerBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollAutomowers(bridge), 1,
                    pollingIntervalToUse, TimeUnit.SECONDS);
        }
    }

    private void stopAutomowerBridgePolling(AutomowerBridge bridge) {
        ScheduledFuture<?> currentPollingJob = automowerBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            automowerBridgePollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable AutomowerBridge getAutomowerBridge() {
        return bridge;
    }

    public Optional<MowerListResult> getAutomowers() {
        AutomowerBridge currentBridge = bridge;
        if (currentBridge == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(currentBridge.getAutomowers());
        } catch (AutomowerCommunicationException e) {
            logger.debug("Bridge cannot get list of available automowers {}", e.getMessage());
            return Optional.empty();
        }
    }
}
