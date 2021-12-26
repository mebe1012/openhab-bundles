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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;
import static org.openhab.binding.avmfritz.internal.dto.HeatingModel.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.config.AVMFritzDeviceConfiguration;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.AlertModel;
import org.openhab.binding.avmfritz.internal.dto.BatteryModel;
import org.openhab.binding.avmfritz.internal.dto.ColorControlModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.openhab.binding.avmfritz.internal.dto.HeatingModel;
import org.openhab.binding.avmfritz.internal.dto.HeatingModel.NextChangeModel;
import org.openhab.binding.avmfritz.internal.dto.HumidityModel;
import org.openhab.binding.avmfritz.internal.dto.LevelControlModel;
import org.openhab.binding.avmfritz.internal.dto.PowerMeterModel;
import org.openhab.binding.avmfritz.internal.dto.SimpleOnOffModel;
import org.openhab.binding.avmfritz.internal.dto.SwitchModel;
import org.openhab.binding.avmfritz.internal.dto.TemperatureModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback.BlindCommand;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! thing. Handles commands, which are sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 */
@NonNullByDefault
public abstract class AVMFritzBaseThingHandler extends BaseThingHandler implements FritzAhaStatusListener {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzBaseThingHandler.class);

    /**
     * keeps track of the current state for handling of increase/decrease
     */
    private AVMFritzBaseModel currentDevice = new DeviceModel();
    private @Nullable String identifier;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! device
     */
    public AVMFritzBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final AVMFritzDeviceConfiguration config = getConfigAs(AVMFritzDeviceConfiguration.class);
        final String newIdentifier = config.ain;
        if (newIdentifier == null || newIdentifier.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'ain' parameter must be configured.");
        } else {
            this.identifier = newIdentifier;
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void onDeviceAdded(AVMFritzBaseModel device) {
        // nothing to do
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        if (thing.getUID().equals(thingUID)) {
            logger.debug("Update thing '{}' with device model: {}", thingUID, device);
            if (device.getPresent() == 1) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present");
            }
            currentDevice = device;

            updateProperties(device, editProperties());

            if (device.isPowermeter()) {
                updatePowermeter(device.getPowermeter());
            }
            if (device.isSwitchableOutlet()) {
                updateSwitchableOutlet(device.getSwitch());
            }
            if (device.isHeatingThermostat()) {
                updateHeatingThermostat(device.getHkr());
            }
            if (device instanceof DeviceModel) {
                DeviceModel deviceModel = (DeviceModel) device;
                if (deviceModel.isTemperatureSensor()) {
                    updateTemperatureSensor(deviceModel.getTemperature());
                }
                if (deviceModel.isHumiditySensor()) {
                    updateHumiditySensor(deviceModel.getHumidity());
                }
                if (deviceModel.isHANFUNAlarmSensor()) {
                    updateHANFUNAlarmSensor(deviceModel.getAlert());
                }
                if (deviceModel.isHANFUNBlinds()) {
                    updateLevelControl(deviceModel.getLevelControlModel());
                } else if (deviceModel.isColorLight()) {
                    updateColorLight(deviceModel.getColorControlModel(), deviceModel.getLevelControlModel());
                } else if (deviceModel.isDimmableLight() && !deviceModel.isHANFUNBlinds()) {
                    updateDimmableLight(deviceModel.getLevelControlModel());
                } else if (deviceModel.isHANFUNUnit() && deviceModel.isHANFUNOnOff()) {
                    updateSimpleOnOffUnit(deviceModel.getSimpleOnOffUnit());
                }
            }
        }
    }

    private void updateHANFUNAlarmSensor(@Nullable AlertModel alertModel) {
        if (alertModel != null) {
            updateThingChannelState(CHANNEL_CONTACT_STATE,
                    AlertModel.ON.equals(alertModel.getState()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }
    }

    protected void updateTemperatureSensor(@Nullable TemperatureModel temperatureModel) {
        if (temperatureModel != null) {
            updateThingChannelState(CHANNEL_TEMPERATURE,
                    new QuantityType<>(temperatureModel.getCelsius(), SIUnits.CELSIUS));
            updateThingChannelConfiguration(CHANNEL_TEMPERATURE, CONFIG_CHANNEL_TEMP_OFFSET,
                    temperatureModel.getOffset());
        }
    }

    protected void updateHumiditySensor(@Nullable HumidityModel humidityModel) {
        if (humidityModel != null) {
            updateThingChannelState(CHANNEL_HUMIDITY,
                    new QuantityType<>(humidityModel.getRelativeHumidity(), Units.PERCENT));
        }
    }

    protected void updateLevelControl(@Nullable LevelControlModel levelControlModel) {
        if (levelControlModel != null) {
            updateThingChannelState(CHANNEL_ROLLERSHUTTER, new PercentType(levelControlModel.getLevelPercentage()));
        }
    }

    private void updateDimmableLight(@Nullable LevelControlModel levelControlModel) {
        if (levelControlModel != null) {
            updateThingChannelState(CHANNEL_BRIGHTNESS, new PercentType(levelControlModel.getLevelPercentage()));
        }
    }

    private void updateColorLight(@Nullable ColorControlModel colorControlModel,
            @Nullable LevelControlModel levelControlModel) {
        if (colorControlModel != null && levelControlModel != null) {
            DecimalType hue = new DecimalType(colorControlModel.hue);
            PercentType saturation = ColorControlModel.toPercent(colorControlModel.saturation);
            PercentType brightness = new PercentType(levelControlModel.getLevelPercentage());
            updateThingChannelState(CHANNEL_COLOR, new HSBType(hue, saturation, brightness));
        }
    }

    private void updateHeatingThermostat(@Nullable HeatingModel heatingModel) {
        if (heatingModel != null) {
            updateThingChannelState(CHANNEL_MODE, new StringType(heatingModel.getMode()));
            updateThingChannelState(CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(heatingModel.getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(heatingModel.getDevicelock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_ACTUALTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getTist()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_SETTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getTsoll()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_ECOTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getAbsenk()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_COMFORTTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getKomfort()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_RADIATOR_MODE, new StringType(heatingModel.getRadiatorMode()));
            NextChangeModel nextChange = heatingModel.getNextchange();
            if (nextChange != null) {
                int endPeriod = nextChange.getEndperiod();
                updateThingChannelState(CHANNEL_NEXT_CHANGE, endPeriod == 0 ? UnDefType.UNDEF
                        : new DateTimeType(
                                ZonedDateTime.ofInstant(Instant.ofEpochSecond(endPeriod), ZoneId.systemDefault())));
                BigDecimal nextTemperature = nextChange.getTchange();
                updateThingChannelState(CHANNEL_NEXTTEMP, TEMP_FRITZ_UNDEFINED.equals(nextTemperature) ? UnDefType.UNDEF
                        : new QuantityType<>(toCelsius(nextTemperature), SIUnits.CELSIUS));
            }
            updateBattery(heatingModel);
        }
    }

    protected void updateBattery(BatteryModel batteryModel) {
        BigDecimal batteryLevel = batteryModel.getBattery();
        updateThingChannelState(CHANNEL_BATTERY,
                batteryLevel == null ? UnDefType.UNDEF : new DecimalType(batteryLevel));
        BigDecimal lowBattery = batteryModel.getBatterylow();
        if (lowBattery == null) {
            updateThingChannelState(CHANNEL_BATTERY_LOW, UnDefType.UNDEF);
        } else {
            updateThingChannelState(CHANNEL_BATTERY_LOW, OnOffType.from(BatteryModel.BATTERY_ON.equals(lowBattery)));
        }
    }

    private void updateSimpleOnOffUnit(@Nullable SimpleOnOffModel simpleOnOffUnit) {
        if (simpleOnOffUnit != null) {
            updateThingChannelState(CHANNEL_ON_OFF, OnOffType.from(simpleOnOffUnit.state));
        }
    }

    private void updateSwitchableOutlet(@Nullable SwitchModel switchModel) {
        if (switchModel != null) {
            updateThingChannelState(CHANNEL_MODE, new StringType(switchModel.getMode()));
            updateThingChannelState(CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getDevicelock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            BigDecimal state = switchModel.getState();
            if (state == null) {
                updateThingChannelState(CHANNEL_OUTLET, UnDefType.UNDEF);
            } else {
                updateThingChannelState(CHANNEL_OUTLET, OnOffType.from(SwitchModel.ON.equals(state)));
            }
        }
    }

    private void updatePowermeter(@Nullable PowerMeterModel powerMeterModel) {
        if (powerMeterModel != null) {
            updateThingChannelState(CHANNEL_ENERGY, new QuantityType<>(powerMeterModel.getEnergy(), Units.WATT_HOUR));
            updateThingChannelState(CHANNEL_POWER, new QuantityType<>(powerMeterModel.getPower(), Units.WATT));
            updateThingChannelState(CHANNEL_VOLTAGE, new QuantityType<>(powerMeterModel.getVoltage(), Units.VOLT));
        }
    }

    /**
     * Updates thing properties.
     *
     * @param device the {@link AVMFritzBaseModel}
     * @param editProperties map of existing properties
     */
    protected void updateProperties(AVMFritzBaseModel device, Map<String, String> editProperties) {
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        updateProperties(editProperties);
    }

    /**
     * Updates thing channels and creates dynamic channels if missing.
     *
     * @param channelId ID of the channel to be updated.
     * @param state State to be set.
     */
    protected void updateThingChannelState(String channelId, State state) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist, recreating thing.", channelId, thing.getUID());
            createChannel(channelId);
        }
    }

    /**
     * Creates a {@link ChannelTypeUID} from the given channel id.
     *
     * @param channelId ID of the channel type UID to be created.
     * @return the channel type UID
     */
    private ChannelTypeUID createChannelTypeUID(String channelId) {
        int pos = channelId.indexOf(ChannelUID.CHANNEL_GROUP_SEPARATOR);
        String id = pos > -1 ? channelId.substring(pos + 1) : channelId;
        return CHANNEL_BATTERY.equals(id) ? DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID()
                : new ChannelTypeUID(BINDING_ID, id);
    }

    /**
     * Creates new channels for the thing.
     *
     * @param channelId ID of the channel to be created.
     */
    private void createChannel(String channelId) {
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID = createChannelTypeUID(channelId);
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
        }
    }

    /**
     * Updates thing channel configurations.
     *
     * @param channelId ID of the channel which configuration to be updated.
     * @param configId ID of the configuration to be updated.
     * @param value Value to be set.
     */
    protected void updateThingChannelConfiguration(String channelId, String configId, Object value) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Configuration editConfig = channel.getConfiguration();
            editConfig.put(configId, value);
        }
    }

    @Override
    public void onDeviceGone(ThingUID thingUID) {
        if (thing.getUID().equals(thingUID)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device not present in response");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
            return;
        }
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        String ain = getIdentifier();
        if (ain == null) {
            logger.debug("Cannot handle command '{}' because AIN is missing", command);
            return;
        }
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_TEMPERATURE:
            case CHANNEL_HUMIDITY:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_VOLTAGE:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_NEXT_CHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
            case CHANNEL_BATTERY_LOW:
            case CHANNEL_CONTACT_STATE:
            case CHANNEL_LAST_CHANGE:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
                break;
            case CHANNEL_OUTLET:
            case CHANNEL_ON_OFF:
                if (command instanceof OnOffType) {
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_COLOR:
            case CHANNEL_BRIGHTNESS:
                BigDecimal brightness = null;
                if (command instanceof HSBType) {
                    HSBType hsbType = (HSBType) command;
                    brightness = hsbType.getBrightness().toBigDecimal();
                    fritzBox.setHueAndSaturation(ain, hsbType.getHue().intValue(),
                            ColorControlModel.fromPercent(hsbType.getSaturation()), 0);
                } else if (command instanceof PercentType) {
                    brightness = ((PercentType) command).toBigDecimal();
                } else if (command instanceof OnOffType) {
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                }
                if (brightness != null) {
                    fritzBox.setLevelPercentage(ain, brightness);
                }
                break;
            case CHANNEL_SETTEMP:
                BigDecimal temperature = null;
                if (command instanceof DecimalType) {
                    temperature = normalizeCelsius(((DecimalType) command).toBigDecimal());
                } else if (command instanceof QuantityType) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Temperature> convertedCommand = ((QuantityType<Temperature>) command)
                            .toUnit(SIUnits.CELSIUS);
                    if (convertedCommand != null) {
                        temperature = normalizeCelsius(convertedCommand.toBigDecimal());
                    } else {
                        logger.warn("Unable to convert unit from '{}' to '{}'. Skipping command.",
                                ((QuantityType<?>) command).getUnit(), SIUnits.CELSIUS);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    temperature = currentDevice.getHkr().getTsoll();
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        temperature.add(BigDecimal.ONE);
                    } else {
                        temperature.subtract(BigDecimal.ONE);
                    }
                } else if (command instanceof OnOffType) {
                    temperature = OnOffType.ON.equals(command) ? TEMP_FRITZ_ON : TEMP_FRITZ_OFF;
                }
                if (temperature != null) {
                    fritzBox.setSetTemp(ain, fromCelsius(temperature));
                    HeatingModel heatingModel = currentDevice.getHkr();
                    heatingModel.setTsoll(temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(heatingModel.getRadiatorMode()));
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                BigDecimal targetTemperature = null;
                if (command instanceof StringType) {
                    switch (command.toString()) {
                        case MODE_ON:
                            targetTemperature = TEMP_FRITZ_ON;
                            break;
                        case MODE_OFF:
                            targetTemperature = TEMP_FRITZ_OFF;
                            break;
                        case MODE_COMFORT:
                            targetTemperature = currentDevice.getHkr().getKomfort();
                            break;
                        case MODE_ECO:
                            targetTemperature = currentDevice.getHkr().getAbsenk();
                            break;
                        case MODE_BOOST:
                            targetTemperature = TEMP_FRITZ_MAX;
                            break;
                        case MODE_UNKNOWN:
                        case MODE_WINDOW_OPEN:
                            logger.debug("Command '{}' is a read-only command for channel {}.", command, channelId);
                            break;
                    }
                    if (targetTemperature != null) {
                        fritzBox.setSetTemp(ain, targetTemperature);
                        currentDevice.getHkr().setTsoll(targetTemperature);
                        updateState(CHANNEL_SETTEMP, new QuantityType<>(toCelsius(targetTemperature), SIUnits.CELSIUS));
                    }
                }
                break;
            case CHANNEL_ROLLERSHUTTER:
                if (command instanceof StopMoveType) {
                    StopMoveType rollershutterCommand = (StopMoveType) command;
                    if (StopMoveType.STOP.equals(rollershutterCommand)) {
                        fritzBox.setBlind(ain, BlindCommand.STOP);
                    } else {
                        logger.debug("Received unknown rollershutter StopMove command MOVE");
                    }
                } else if (command instanceof UpDownType) {
                    UpDownType rollershutterCommand = (UpDownType) command;
                    if (UpDownType.UP.equals(rollershutterCommand)) {
                        fritzBox.setBlind(ain, BlindCommand.OPEN);
                    } else {
                        fritzBox.setBlind(ain, BlindCommand.CLOSE);
                    }
                } else if (command instanceof PercentType) {
                    BigDecimal levelPercentage = ((PercentType) command).toBigDecimal();
                    fritzBox.setLevelPercentage(ain, levelPercentage);
                } else {
                    logger.debug("Received unknown rollershutter command type '{}'", command.toString());
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }
    }

    /**
     * Handles a command for a given action.
     *
     * @param action
     * @param duration
     */
    protected void handleAction(String action, long duration) {
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle action '{}' because connection is missing", action);
            return;
        }
        String ain = getIdentifier();
        if (ain == null) {
            logger.debug("Cannot handle action '{}' because AIN is missing", action);
            return;
        }
        if (duration < 0 || 86400 < duration) {
            throw new IllegalArgumentException("Duration must not be less than zero or greater than 86400");
        }
        switch (action) {
            case MODE_BOOST:
                fritzBox.setBoostMode(ain,
                        duration > 0 ? ZonedDateTime.now().plusSeconds(duration).toEpochSecond() : 0);
                break;
            case MODE_WINDOW_OPEN:
                fritzBox.setWindowOpenMode(ain,
                        duration > 0 ? ZonedDateTime.now().plusSeconds(duration).toEpochSecond() : 0);
                break;
            default:
                logger.debug("Received unknown action '{}'", action);
                break;
        }
    }

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    private @Nullable FritzAhaWebInterface getWebInterface() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AVMFritzBaseBridgeHandler) {
                return ((AVMFritzBaseBridgeHandler) handler).getWebInterface();
            }
        }
        return null;
    }

    /**
     * Handles a refresh command.
     */
    private void handleRefreshCommand() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AVMFritzBaseBridgeHandler) {
                ((AVMFritzBaseBridgeHandler) handler).handleRefreshCommand();
            }
        }
    }

    /**
     * Returns the AIN.
     *
     * @return the AIN
     */
    public @Nullable String getIdentifier() {
        return identifier;
    }
}
