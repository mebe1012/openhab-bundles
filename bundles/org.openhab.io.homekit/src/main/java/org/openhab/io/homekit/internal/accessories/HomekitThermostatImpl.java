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
package org.openhab.io.homekit.internal.accessories;

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.ThermostatAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TargetTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TemperatureDisplayUnitEnum;
import io.github.hapjava.services.impl.ThermostatService;

/**
 * Implements Thermostat as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Current Temperature: Number type</li>
 * <li>Target Temperature: Number type</li>
 * <li>Current Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * <li>Target Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * </ul>
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitThermostatImpl extends AbstractHomekitAccessoryImpl implements ThermostatAccessory {
    private final Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);
    private final Map<CurrentHeatingCoolingStateEnum, String> currentHeatingCoolingStateMapping;
    private final Map<TargetHeatingCoolingStateEnum, String> targetHeatingCoolingStateMapping;
    private final List<CurrentHeatingCoolingStateEnum> customCurrentHeatingCoolingStateList;
    private final List<TargetHeatingCoolingStateEnum> customTargetHeatingCoolingStateList;

    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        currentHeatingCoolingStateMapping = new EnumMap<>(CurrentHeatingCoolingStateEnum.class);
        currentHeatingCoolingStateMapping.put(CurrentHeatingCoolingStateEnum.OFF, settings.thermostatCurrentModeOff);
        currentHeatingCoolingStateMapping.put(CurrentHeatingCoolingStateEnum.COOL,
                settings.thermostatCurrentModeCooling);
        currentHeatingCoolingStateMapping.put(CurrentHeatingCoolingStateEnum.HEAT,
                settings.thermostatCurrentModeHeating);
        targetHeatingCoolingStateMapping = new EnumMap<>(TargetHeatingCoolingStateEnum.class);
        targetHeatingCoolingStateMapping.put(TargetHeatingCoolingStateEnum.OFF, settings.thermostatTargetModeOff);
        targetHeatingCoolingStateMapping.put(TargetHeatingCoolingStateEnum.COOL, settings.thermostatTargetModeCool);
        targetHeatingCoolingStateMapping.put(TargetHeatingCoolingStateEnum.HEAT, settings.thermostatTargetModeHeat);
        targetHeatingCoolingStateMapping.put(TargetHeatingCoolingStateEnum.AUTO, settings.thermostatTargetModeAuto);
        customCurrentHeatingCoolingStateList = new ArrayList<>();
        customTargetHeatingCoolingStateList = new ArrayList<>();
        updateMapping(CURRENT_HEATING_COOLING_STATE, currentHeatingCoolingStateMapping,
                customCurrentHeatingCoolingStateList);
        updateMapping(TARGET_HEATING_COOLING_STATE, targetHeatingCoolingStateMapping,
                customTargetHeatingCoolingStateList);
        this.getServices().add(new ThermostatService(this));
    }

    @Override
    public CurrentHeatingCoolingStateEnum[] getCurrentHeatingCoolingStateValidValues() {
        return customCurrentHeatingCoolingStateList.isEmpty()
                ? currentHeatingCoolingStateMapping.keySet().toArray(new CurrentHeatingCoolingStateEnum[0])
                : customCurrentHeatingCoolingStateList.toArray(new CurrentHeatingCoolingStateEnum[0]);
    }

    @Override
    public TargetHeatingCoolingStateEnum[] getTargetHeatingCoolingStateValidValues() {
        return customTargetHeatingCoolingStateList.isEmpty()
                ? targetHeatingCoolingStateMapping.keySet().toArray(new TargetHeatingCoolingStateEnum[0])
                : customTargetHeatingCoolingStateList.toArray(new TargetHeatingCoolingStateEnum[0]);
    }

    @Override
    public CompletableFuture<CurrentHeatingCoolingStateEnum> getCurrentState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(CURRENT_HEATING_COOLING_STATE,
                currentHeatingCoolingStateMapping, CurrentHeatingCoolingStateEnum.OFF));
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        DecimalType state = getStateAs(HomekitCharacteristicType.CURRENT_TEMPERATURE, DecimalType.class);
        return CompletableFuture.completedFuture(state != null ? convertToCelsius(state.doubleValue()) : 0.0);
    }

    @Override
    public double getMinCurrentTemperature() {
        return convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.MIN_VALUE,
                        BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_MIN_VALUE)).doubleValue());
    }

    @Override
    public double getMaxCurrentTemperature() {
        return convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.MAX_VALUE,
                        BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_MAX_VALUE)).doubleValue());
    }

    @Override
    public double getMinStepCurrentTemperature() {
        return getAccessoryConfiguration(HomekitCharacteristicType.CURRENT_TEMPERATURE, HomekitTaggedItem.STEP,
                BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_STEP)).doubleValue();
    }

    @Override
    public CompletableFuture<TargetHeatingCoolingStateEnum> getTargetState() {
        return CompletableFuture.completedFuture(getKeyFromMapping(TARGET_HEATING_COOLING_STATE,
                targetHeatingCoolingStateMapping, TargetHeatingCoolingStateEnum.OFF));
    }

    @Override
    public CompletableFuture<TemperatureDisplayUnitEnum> getTemperatureDisplayUnit() {
        return CompletableFuture
                .completedFuture(getSettings().useFahrenheitTemperature ? TemperatureDisplayUnitEnum.FAHRENHEIT
                        : TemperatureDisplayUnitEnum.CELSIUS);
    }

    @Override
    public void setTemperatureDisplayUnit(TemperatureDisplayUnitEnum value) {
        // TODO: add support for display unit change
    }

    @Override
    public CompletableFuture<Double> getTargetTemperature() {
        DecimalType state = getStateAs(HomekitCharacteristicType.TARGET_TEMPERATURE, DecimalType.class);
        return CompletableFuture.completedFuture(state != null ? convertToCelsius(state.doubleValue()) : 0.0);
    }

    @Override
    public void setTargetState(TargetHeatingCoolingStateEnum mode) {
        getItem(TARGET_HEATING_COOLING_STATE, StringItem.class)
                .ifPresent(item -> item.send(new StringType(targetHeatingCoolingStateMapping.get(mode))));
    }

    @Override
    public void setTargetTemperature(Double value) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_TEMPERATURE);
        if (characteristic.isPresent()) {
            ((NumberItem) characteristic.get().getItem())
                    .send(new DecimalType(BigDecimal.valueOf(convertFromCelsius(value))));
        } else {
            logger.warn("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_TEMPERATURE);
        }
    }

    @Override
    public double getMinTargetTemperature() {
        return convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.TARGET_TEMPERATURE, HomekitTaggedItem.MIN_VALUE,
                        BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_MIN_VALUE)).doubleValue());
    }

    @Override
    public double getMaxTargetTemperature() {
        return convertToCelsius(
                getAccessoryConfiguration(HomekitCharacteristicType.TARGET_TEMPERATURE, HomekitTaggedItem.MAX_VALUE,
                        BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_MAX_VALUE)).doubleValue());
    }

    @Override
    public double getMinStepTargetTemperature() {
        return getAccessoryConfiguration(HomekitCharacteristicType.TARGET_TEMPERATURE, HomekitTaggedItem.STEP,
                BigDecimal.valueOf(TargetTemperatureCharacteristic.DEFAULT_STEP)).doubleValue();
    }

    @Override
    public void subscribeCurrentState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CURRENT_HEATING_COOLING_STATE, callback);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE, callback);
    }

    @Override
    public void subscribeTargetState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE, callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_TEMPERATURE, callback);
    }

    @Override
    public void subscribeTemperatureDisplayUnit(HomekitCharacteristicChangeCallback callback) {
        // TODO: add support for display unit change
    }

    @Override
    public void unsubscribeCurrentState() {
        unsubscribe(CURRENT_HEATING_COOLING_STATE);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        unsubscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE);
    }

    @Override
    public void unsubscribeTemperatureDisplayUnit() {
        // TODO: add support for display unit change
    }

    @Override
    public void unsubscribeTargetState() {
        unsubscribe(HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
    }

    @Override
    public void unsubscribeTargetTemperature() {
        unsubscribe(HomekitCharacteristicType.TARGET_TEMPERATURE);
    }
}
