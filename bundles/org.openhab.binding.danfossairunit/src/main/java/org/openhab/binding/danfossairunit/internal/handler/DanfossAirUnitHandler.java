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
package org.openhab.binding.danfossairunit.internal.handler;

import static org.openhab.binding.danfossairunit.internal.DanfossAirUnitBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.danfossairunit.internal.Channel;
import org.openhab.binding.danfossairunit.internal.ChannelGroup;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnit;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitCommunicationController;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitConfiguration;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitWriteAccessor;
import org.openhab.binding.danfossairunit.internal.UnexpectedResponseValueException;
import org.openhab.binding.danfossairunit.internal.ValueCache;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossAirUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 * @author Jacob Laursen - Refactoring, bugfixes and enhancements
 */
@NonNullByDefault
public class DanfossAirUnitHandler extends BaseThingHandler {

    private static final int TCP_PORT = 30046;
    private static final int POLLING_INTERVAL_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitHandler.class);
    private @NonNullByDefault({}) DanfossAirUnitConfiguration config;
    private @Nullable ValueCache valueCache;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable DanfossAirUnitCommunicationController communicationController;
    private @Nullable DanfossAirUnit airUnit;

    public DanfossAirUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAllChannels();
        } else {
            try {
                DanfossAirUnit localAirUnit = this.airUnit;
                if (localAirUnit != null) {
                    Channel channel = Channel.getByName(channelUID.getIdWithoutGroup());
                    DanfossAirUnitWriteAccessor writeAccessor = channel.getWriteAccessor();
                    if (writeAccessor != null) {
                        updateState(channelUID, writeAccessor.access(localAirUnit, command));
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE,
                            "@text/offline.connection-not-initialized");
                    return;
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Ignoring unknown channel id: {}", channelUID.getIdWithoutGroup(), e);
            } catch (IOException ioe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(DanfossAirUnitConfiguration.class);
        valueCache = new ValueCache(config.updateUnchangedValuesEveryMillis);
        removeDeprecatedChannels();
        try {
            var localCommunicationController = new DanfossAirUnitCommunicationController(
                    InetAddress.getByName(config.host), TCP_PORT);
            this.communicationController = localCommunicationController;
            var localAirUnit = new DanfossAirUnit(localCommunicationController);
            this.airUnit = localAirUnit;
            scheduler.execute(() -> {
                try {
                    thing.setProperty(PROPERTY_UNIT_NAME, localAirUnit.getUnitName());
                    thing.setProperty(PROPERTY_SERIAL, localAirUnit.getUnitSerialNumber());
                    startPolling();
                    updateStatus(ThingStatus.ONLINE);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                }
            });
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.unknown-host [\"" + config.host + "\"]");
            return;
        }
    }

    private void removeDeprecatedChannels() {
        ChannelGroupUID mainChannelGroupUid = new ChannelGroupUID(thing.getUID(), ChannelGroup.MAIN.getGroupName());
        ChannelUID manualFanSpeedChannelUid = new ChannelUID(mainChannelGroupUid,
                Channel.CHANNEL_MANUAL_FAN_SPEED.getChannelName());
        if (this.isLinked(manualFanSpeedChannelUid)) {
            ChannelUID manualFanStepChannelUid = new ChannelUID(mainChannelGroupUid,
                    Channel.CHANNEL_MANUAL_FAN_STEP.getChannelName());
            logger.warn("Channel '{}' is deprecated, please use '{}' instead.", manualFanSpeedChannelUid,
                    manualFanStepChannelUid);
        } else {
            logger.debug("Removing deprecated unlinked channel '{}'.", manualFanSpeedChannelUid);
            updateThing(editThing().withoutChannel(manualFanSpeedChannelUid).build());
        }
    }

    private void updateAllChannels() {
        DanfossAirUnit localAirUnit = this.airUnit;
        if (localAirUnit == null) {
            return;
        }

        logger.debug("Updating DanfossHRV data '{}'", getThing().getUID());

        for (Channel channel : Channel.values()) {
            if (Thread.interrupted()) {
                logger.debug("Polling thread interrupted...");
                return;
            }
            try {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(),
                        channel.getReadAccessor().access(localAirUnit));
                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (UnexpectedResponseValueException e) {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(), UnDefType.UNDEF);
                logger.debug(
                        "Cannot update channel {}: an unexpected or invalid response has been received from the air unit: {}",
                        channel.getChannelName(), e.getMessage());
                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (IOException e) {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(), UnDefType.UNDEF);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                logger.debug("Cannot update channel {}: an error occurred retrieving the value: {}",
                        channel.getChannelName(), e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss HRV handler '{}'", getThing().getUID());

        stopPolling();

        this.airUnit = null;

        DanfossAirUnitCommunicationController localCommunicationController = this.communicationController;
        if (localCommunicationController != null) {
            localCommunicationController.disconnect();
        }
        this.communicationController = null;
    }

    private synchronized void startPolling() {
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::updateAllChannels, POLLING_INTERVAL_SECONDS,
                config.refreshInterval, TimeUnit.SECONDS);
    }

    private synchronized void stopPolling() {
        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }
        this.pollingJob = null;
    }

    private void updateState(String groupId, String channelId, State state) {
        ValueCache cache = valueCache;
        if (cache == null) {
            return;
        }

        if (cache.updateValue(channelId, state)) {
            updateState(new ChannelUID(thing.getUID(), groupId, channelId), state);
        }
    }
}
