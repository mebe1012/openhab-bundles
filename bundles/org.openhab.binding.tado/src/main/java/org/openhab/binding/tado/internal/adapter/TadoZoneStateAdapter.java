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
package org.openhab.binding.tado.internal.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.api.model.AcPowerDataPoint;
import org.openhab.binding.tado.internal.api.model.ActivityDataPoints;
import org.openhab.binding.tado.internal.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HeatingZoneSetting;
import org.openhab.binding.tado.internal.api.model.HotWaterZoneSetting;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.SensorDataPoints;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureDataPoint;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Adapter from API-level zone state to the binding's item-based zone state.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - Added Low Battery Alarm, A/C Power and Open Window channels
 *
 */
public class TadoZoneStateAdapter {
    private ZoneState zoneState;
    private TemperatureUnit temperatureUnit;

    public TadoZoneStateAdapter(ZoneState zoneState, TemperatureUnit temperatureUnit) {
        this.zoneState = zoneState;
        this.temperatureUnit = temperatureUnit;
    }

    public State getInsideTemperature() {
        SensorDataPoints sensorDataPoints = zoneState.getSensorDataPoints();
        return toTemperatureState(sensorDataPoints.getInsideTemperature(), temperatureUnit);
    }

    public DecimalType getHumidity() {
        SensorDataPoints sensorDataPoints = zoneState.getSensorDataPoints();
        return sensorDataPoints.getHumidity() != null ? toDecimalType(sensorDataPoints.getHumidity().getPercentage())
                : null;
    }

    public DecimalType getHeatingPower() {
        ActivityDataPoints dataPoints = zoneState.getActivityDataPoints();
        return dataPoints.getHeatingPower() != null ? toDecimalType(dataPoints.getHeatingPower().getPercentage())
                : DecimalType.ZERO;
    }

    public OnOffType getAcPower() {
        ActivityDataPoints dataPoints = zoneState.getActivityDataPoints();
        AcPowerDataPoint acPower = dataPoints.getAcPower();
        if (acPower != null) {
            String acPowerValue = acPower.getValue();
            if (acPowerValue != null) {
                return OnOffType.from(acPowerValue);
            }
        }
        return null;
    }

    public StringType getMode() {
        GenericZoneSetting setting = zoneState.getSetting();

        if (!isPowerOn()) {
            return StringType.valueOf(HvacMode.OFF.name());
        } else if (setting.getType() == TadoSystemType.HEATING || setting.getType() == TadoSystemType.HOT_WATER) {
            return StringType.valueOf(HvacMode.HEAT.name());
        } else {
            return StringType.valueOf(((CoolingZoneSetting) setting).getMode().getValue());
        }
    }

    public State getTargetTemperature() {
        switch (zoneState.getSetting().getType()) {
            case HEATING:
                return toTemperatureState(((HeatingZoneSetting) zoneState.getSetting()).getTemperature(),
                        temperatureUnit);
            case AIR_CONDITIONING:
                return toTemperatureState(((CoolingZoneSetting) zoneState.getSetting()).getTemperature(),
                        temperatureUnit);
            case HOT_WATER:
                return toTemperatureState(((HotWaterZoneSetting) zoneState.getSetting()).getTemperature(),
                        temperatureUnit);
            default:
                return UnDefType.UNDEF;
        }
    }

    public State getFanSpeed() {
        if (zoneState.getSetting().getType() == TadoSystemType.AIR_CONDITIONING) {
            CoolingZoneSetting setting = (CoolingZoneSetting) zoneState.getSetting();
            return setting.getFanSpeed() != null ? StringType.valueOf(setting.getFanSpeed().getValue())
                    : UnDefType.NULL;
        } else {
            return UnDefType.UNDEF;
        }
    }

    public State getSwing() {
        if (zoneState.getSetting().getType() == TadoSystemType.AIR_CONDITIONING) {
            CoolingZoneSetting setting = (CoolingZoneSetting) zoneState.getSetting();
            if (setting.getSwing() == null) {
                return UnDefType.NULL;
            } else if (setting.getSwing() == Power.ON) {
                return OnOffType.ON;
            } else {
                return OnOffType.OFF;
            }
        } else {
            return UnDefType.UNDEF;
        }
    }

    public StringType getOperationMode() {
        Overlay overlay = zoneState.getOverlay();
        if (overlay != null) {
            switch (overlay.getTermination().getType()) {
                case MANUAL:
                    return new StringType(OperationMode.MANUAL.name());
                case TIMER:
                    return new StringType(OperationMode.TIMER.name());
                case TADO_MODE:
                    return new StringType(OperationMode.UNTIL_CHANGE.name());
            }
        }

        return new StringType(OperationMode.SCHEDULE.name());
    }

    public DecimalType getRemainingTimerDuration() {
        Overlay overlay = zoneState.getOverlay();
        if (overlay != null && overlay.getTermination().getType() == OverlayTerminationConditionType.TIMER) {
            TimerTerminationCondition timerTerminationCondition = (TimerTerminationCondition) overlay.getTermination();

            Integer remainingTimeInSeconds = timerTerminationCondition.getRemainingTimeInSeconds();
            // If there's a timer overlay, then the timer should never be smaller than 1
            return new DecimalType((int) Math.max(1.0, Math.round(remainingTimeInSeconds / 60.0)));
        }

        return new DecimalType();
    }

    public State getOverlayExpiration() {
        Overlay overlay = zoneState.getOverlay();
        if (overlay == null || overlay.getTermination().getProjectedExpiry() == null) {
            return UnDefType.UNDEF;
        }

        return toDateTimeType(overlay.getTermination().getProjectedExpiry());
    }

    public boolean isPowerOn() {
        Power power = Power.OFF;
        GenericZoneSetting setting = zoneState.getSetting();

        switch (setting.getType()) {
            case HEATING:
                power = ((HeatingZoneSetting) setting).getPower();
                break;
            case AIR_CONDITIONING:
                power = ((CoolingZoneSetting) setting).getPower();
                break;
            case HOT_WATER:
                power = ((HotWaterZoneSetting) setting).getPower();
                break;
        }

        return power.getValue().equals("ON");
    }

    private static DecimalType toDecimalType(double value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return new DecimalType(decimal);
    }

    private static DateTimeType toDateTimeType(OffsetDateTime offsetDateTime) {
        return new DateTimeType(offsetDateTime.toZonedDateTime());
    }

    private static State toTemperatureState(TemperatureObject temperature, TemperatureUnit temperatureUnit) {
        if (temperature == null) {
            return UnDefType.NULL;
        }

        return temperatureUnit == TemperatureUnit.FAHRENHEIT
                ? new QuantityType<>(temperature.getFahrenheit(), ImperialUnits.FAHRENHEIT)
                : new QuantityType<>(temperature.getCelsius(), SIUnits.CELSIUS);
    }

    private static State toTemperatureState(TemperatureDataPoint temperature, TemperatureUnit temperatureUnit) {
        if (temperature == null) {
            return UnDefType.NULL;
        }

        return temperatureUnit == TemperatureUnit.FAHRENHEIT
                ? new QuantityType<>(temperature.getFahrenheit(), ImperialUnits.FAHRENHEIT)
                : new QuantityType<>(temperature.getCelsius(), SIUnits.CELSIUS);
    }

    public State getOpenWindowDetected() {
        Boolean openWindowDetected = zoneState.isOpenWindowDetected();
        if (openWindowDetected != null) {
            return OnOffType.from(openWindowDetected);
        }
        return OnOffType.OFF;
    }
}
