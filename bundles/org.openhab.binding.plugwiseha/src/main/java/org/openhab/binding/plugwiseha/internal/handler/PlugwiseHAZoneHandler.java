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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.BRIDGE_OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHAThingConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHAZoneHandler} class is responsible for handling commands
 * and status updates for the Plugwise Home Automation zones/locations.
 * Extends @{link PlugwiseHABaseHandler}
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 *
 */

@NonNullByDefault
public class PlugwiseHAZoneHandler extends PlugwiseHABaseHandler<Location, PlugwiseHAThingConfig> {

    private @Nullable Location location;
    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAZoneHandler.class);

    // Constructor

    public PlugwiseHAZoneHandler(Thing thing) {
        super(thing);
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return PlugwiseHABindingConstants.THING_TYPE_ZONE.equals(thingTypeUID);
    }

    // Overrides

    @Override
    protected synchronized void initialize(PlugwiseHAThingConfig config, PlugwiseHABridgeHandler bridgeHandler) {
        if (thing.getStatus() == INITIALIZING) {
            logger.debug("Initializing Plugwise Home Automation zone handler with config = {}", config);
            if (!config.isValid()) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR,
                        "Invalid configuration for Plugwise Home Automation zone handler.");
                return;
            }

            try {
                PlugwiseHAController controller = bridgeHandler.getController();
                if (controller != null) {
                    this.location = getEntity(controller, true);
                    if (this.location != null) {
                        setLocationProperties();
                        updateStatus(ONLINE);
                    } else {
                        updateStatus(OFFLINE);
                    }
                } else {
                    updateStatus(OFFLINE, BRIDGE_OFFLINE);
                }
            } catch (PlugwiseHAException e) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected @Nullable Location getEntity(PlugwiseHAController controller, Boolean forceRefresh)
            throws PlugwiseHAException {
        PlugwiseHAThingConfig config = getPlugwiseThingConfig();
        Location location = controller.getLocation(config.getId(), forceRefresh);

        return location;
    }

    @Override
    protected void handleCommand(Location entity, ChannelUID channelUID, Command command) throws PlugwiseHAException {
        String channelID = channelUID.getIdWithoutGroup();
        PlugwiseHABridgeHandler bridge = this.getPlugwiseHABridge();
        if (bridge != null) {
            PlugwiseHAController controller = bridge.getController();
            if (controller != null) {
                switch (channelID) {
                    case ZONE_SETPOINT_CHANNEL:
                        if (command instanceof QuantityType) {
                            Unit<Temperature> unit = entity.getSetpointTemperatureUnit().orElse(UNIT_CELSIUS)
                                    .equals(UNIT_CELSIUS) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
                            QuantityType<?> state = ((QuantityType<?>) command).toUnit(unit);
                            if (state != null) {
                                try {
                                    controller.setLocationThermostat(entity, state.doubleValue());
                                } catch (PlugwiseHAException e) {
                                    logger.warn("Unable to update setpoint for zone '{}': {} -> {}", entity.getName(),
                                            entity.getSetpointTemperature().orElse(null), state.doubleValue());
                                }
                            }
                        }
                        break;
                    case ZONE_PREHEAT_CHANNEL:
                        if (command instanceof OnOffType) {
                            try {
                                controller.setPreHeating(entity, command == OnOffType.ON);
                            } catch (PlugwiseHAException e) {
                                logger.warn("Unable to switch zone pre heating {} for zone '{}'", (State) command,
                                        entity.getName());
                            }
                        }
                        break;
                    default:
                        logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
                }
            }
        }
    }

    private State getDefaultState(String channelID) {
        State state = UnDefType.NULL;
        switch (channelID) {
            case ZONE_PREHEAT_CHANNEL:
            case ZONE_PRESETSCENE_CHANNEL:
            case ZONE_SETPOINT_CHANNEL:
            case ZONE_TEMPERATURE_CHANNEL:
                state = UnDefType.NULL;
                break;
        }
        return state;
    }

    @Override
    protected void refreshChannel(Location entity, ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        State state = getDefaultState(channelID);

        switch (channelID) {
            case ZONE_PREHEAT_CHANNEL:
                Optional<Boolean> preHeatState = entity.getPreHeatState();
                if (preHeatState.isPresent()) {
                    state = OnOffType.from(preHeatState.get());
                }
                break;
            case ZONE_PRESETSCENE_CHANNEL:
                state = new StringType(entity.getPreset());
                break;
            case ZONE_SETPOINT_CHANNEL:
                if (entity.getSetpointTemperature().isPresent()) {
                    Unit<Temperature> unit = entity.getSetpointTemperatureUnit().orElse(UNIT_CELSIUS)
                            .equals(UNIT_CELSIUS) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
                    state = new QuantityType<Temperature>(entity.getSetpointTemperature().get(), unit);
                }
                break;
            case ZONE_TEMPERATURE_CHANNEL:
                if (entity.getTemperature().isPresent()) {
                    Unit<Temperature> unit = entity.getTemperatureUnit().orElse(UNIT_CELSIUS).equals(UNIT_CELSIUS)
                            ? SIUnits.CELSIUS
                            : ImperialUnits.FAHRENHEIT;
                    state = new QuantityType<Temperature>(entity.getTemperature().get(), unit);
                }
                break;
            default:
                break;
        }

        if (state != UnDefType.NULL) {
            updateState(channelID, state);
        }
    }

    protected void setLocationProperties() {
        if (this.location != null) {
            Map<String, String> properties = editProperties();

            Location localLocation = this.location;
            if (localLocation != null) {
                properties.put(PlugwiseHABindingConstants.LOCATION_PROPERTY_DESCRIPTION,
                        localLocation.getDescription());
                properties.put(PlugwiseHABindingConstants.LOCATION_PROPERTY_TYPE, localLocation.getType());
                properties.put(PlugwiseHABindingConstants.LOCATION_PROPERTY_FUNCTIONALITIES,
                        String.join(", ", localLocation.getActuatorFunctionalities().keySet()));
            }

            updateProperties(properties);
        }
    }
}
