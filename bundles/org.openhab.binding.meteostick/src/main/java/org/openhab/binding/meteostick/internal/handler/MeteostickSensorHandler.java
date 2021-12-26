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
package org.openhab.binding.meteostick.internal.handler;

import static org.openhab.binding.meteostick.internal.MeteostickBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 * @author John Cocula - Added variable spoon size, UoM, wind stats, bug fixes
 */
public class MeteostickSensorHandler extends BaseThingHandler implements MeteostickEventListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DAVIS);

    private final Logger logger = LoggerFactory.getLogger(MeteostickSensorHandler.class);

    private int channel = 0;
    private BigDecimal spoon = new BigDecimal(PARAMETER_SPOON_DEFAULT);
    private MeteostickBridgeHandler bridgeHandler;
    private RainHistory rainHistory = new RainHistory(HOUR_IN_MSEC);
    private WindHistory windHistory = new WindHistory(2 * 60 * 1000); // 2 minutes
    private ScheduledFuture<?> rainHourlyJob;
    private ScheduledFuture<?> wind2MinJob;
    private ScheduledFuture<?> offlineTimerJob;

    private Date lastData;

    public MeteostickSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick handler.");

        channel = ((BigDecimal) getConfig().get(PARAMETER_CHANNEL)).intValue();

        spoon = (BigDecimal) getConfig().get(PARAMETER_SPOON);
        if (spoon == null) {
            spoon = new BigDecimal(PARAMETER_SPOON_DEFAULT);
        }
        logger.debug("Initializing MeteoStick handler - Channel {}, Spoon size {} mm.", channel, spoon);

        Runnable rainRunnable = () -> {
            BigDecimal rainfall = rainHistory.getTotal(spoon);
            rainfall.setScale(1, RoundingMode.DOWN);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_LASTHOUR),
                    new QuantityType<>(rainfall, MILLI(METRE)));
        };

        // Scheduling a job on each hour to update the last hour rainfall
        long start = HOUR_IN_SEC - ((System.currentTimeMillis() % HOUR_IN_MSEC) / 1000);
        rainHourlyJob = scheduler.scheduleWithFixedDelay(rainRunnable, start, HOUR_IN_SEC, TimeUnit.SECONDS);

        Runnable windRunnable = () -> {
            WindStats stats = windHistory.getStats();
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED_LAST2MIN_AVERAGE),
                    new QuantityType<>(stats.averageSpeed, METRE_PER_SECOND));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED_LAST2MIN_MAXIMUM),
                    new QuantityType<>(stats.maxSpeed, METRE_PER_SECOND));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_DIRECTION_LAST2MIN_AVERAGE),
                    new QuantityType<>(stats.averageDirection, DEGREE_ANGLE));
        };

        // Scheduling a job to run every two minutes to update wind statistics
        wind2MinJob = scheduler.scheduleWithFixedDelay(windRunnable, 2, 2, TimeUnit.MINUTES);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (rainHourlyJob != null) {
            rainHourlyJob.cancel(true);
        }

        if (wind2MinJob != null) {
            wind2MinJob.cancel(true);
        }

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        if (bridgeHandler != null) {
            bridgeHandler.unsubscribeEvents(channel, this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MeteoStick handler {}: bridgeStatusChanged to {}", channel, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            logger.debug("MeteoStick handler {}: bridgeStatusChanged but bridge offline", channel);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        bridgeHandler = (MeteostickBridgeHandler) getBridge().getHandler();

        if (channel != 0) {
            if (bridgeHandler != null) {
                bridgeHandler.subscribeEvents(channel, this);
            }
        }

        // Put the thing online and start our "no data" timer
        updateStatus(ThingStatus.ONLINE);
        startTimeoutCheck();
    }

    private void processSignalStrength(String dbmString) {
        double dbm = Double.parseDouble(dbmString);
        int strength;

        if (dbm > -60) {
            strength = 4;
        } else if (dbm > -70) {
            strength = 3;
        } else if (dbm > -80) {
            strength = 2;
        } else if (dbm > -90) {
            strength = 1;
        } else {
            strength = 0;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SIGNAL_STRENGTH), new DecimalType(strength));
    }

    private void processBattery(boolean batteryLow) {
        OnOffType state = batteryLow ? OnOffType.ON : OnOffType.OFF;

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LOW_BATTERY), state);
    }

    @Override
    public void onDataReceived(String[] data) {
        logger.debug("MeteoStick received channel {}: {}", channel, data);
        updateStatus(ThingStatus.ONLINE);
        lastData = new Date();

        startTimeoutCheck();

        switch (data[0]) {
            case "R": // Rain
                int rain = Integer.parseInt(data[2]);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_RAW), new DecimalType(rain));
                processSignalStrength(data[3]);
                processBattery(data.length == 5);

                rainHistory.put(rain);

                BigDecimal rainfall = rainHistory.getTotal(spoon);
                rainfall.setScale(1, RoundingMode.DOWN);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_CURRENTHOUR),
                        new QuantityType<>(rainfall, MILLI(METRE)));
                break;
            case "W": // Wind
                BigDecimal windSpeed = new BigDecimal(data[2]);
                int windDirection = Integer.parseInt(data[3]);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED),
                        new QuantityType<>(windSpeed, METRE_PER_SECOND));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_DIRECTION),
                        new QuantityType<>(windDirection, DEGREE_ANGLE));

                windHistory.put(windSpeed, windDirection);

                processSignalStrength(data[4]);
                processBattery(data.length == 6);
                break;
            case "T": // Temperature
                BigDecimal temperature = new BigDecimal(data[2]);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTDOOR_TEMPERATURE),
                        new QuantityType<>(temperature.setScale(1), CELSIUS));

                BigDecimal humidity = new BigDecimal(data[3]);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY),
                        new DecimalType(humidity.setScale(1)));

                processSignalStrength(data[4]);
                processBattery(data.length == 6);
                break;
            case "P": // Solar panel power
                BigDecimal power = new BigDecimal(data[2]);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SOLAR_POWER),
                        new DecimalType(power.setScale(1)));

                processSignalStrength(data[3]);
                processBattery(data.length == 5);
                break;
        }
    }

    class SlidingTimeWindow<T> {
        private long period = 0;
        protected final SortedMap<Long, T> storage = Collections.synchronizedSortedMap(new TreeMap<>());

        /**
         *
         * @param period window period in milliseconds
         */
        public SlidingTimeWindow(long period) {
            this.period = period;
        }

        public void put(T value) {
            storage.put(System.currentTimeMillis(), value);
        }

        public void removeOldEntries() {
            long old = System.currentTimeMillis() - period;
            synchronized (storage) {
                for (Iterator<Long> iterator = storage.keySet().iterator(); iterator.hasNext();) {
                    long time = iterator.next();
                    if (time < old) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    class RainHistory extends SlidingTimeWindow<Integer> {

        public RainHistory(long period) {
            super(period);
        }

        public BigDecimal getTotal(BigDecimal spoon) {
            removeOldEntries();

            int least = -1;
            int total = 0;

            synchronized (storage) {
                for (int value : storage.values()) {

                    /*
                     * Rain counters have been seen to wrap at 127 and also at 255.
                     * The Meteostick documentation only mentions 255 at the time of
                     * this writing. This potential difference is solved by having
                     * all rain counters wrap at 127 (0x7F) by removing the high bit.
                     */
                    value &= 0x7F;

                    if (least == -1) {
                        least = value;
                        continue;
                    }

                    if (value < least) {
                        total = 128 - least + value;
                    } else {
                        total = value - least;
                    }
                }
            }

            return BigDecimal.valueOf(total).multiply(spoon);
        }
    }

    /**
     * Store the wind direction as an east-west vector and a north-south vector
     * so that an average direction can be calculated based on the wind speed
     * at the time of the direction sample.
     */
    class WindSample {
        double speed;
        double ewVector;
        double nsVector;

        public WindSample(BigDecimal speed, int directionDegrees) {
            this.speed = speed.doubleValue();
            double direction = Math.toRadians(directionDegrees);
            this.ewVector = this.speed * Math.sin(direction);
            this.nsVector = this.speed * Math.cos(direction);
        }
    }

    class WindStats {
        BigDecimal averageSpeed;
        int averageDirection;
        BigDecimal maxSpeed;
    }

    class WindHistory extends SlidingTimeWindow<WindSample> {

        public WindHistory(long period) {
            super(period);
        }

        public void put(BigDecimal speed, int directionDegrees) {
            put(new WindSample(speed, directionDegrees));
        }

        public WindStats getStats() {
            removeOldEntries();

            double ewSum = 0;
            double nsSum = 0;
            double totalSpeed = 0;
            double maxSpeed = 0;
            int size = 0;
            synchronized (storage) {
                size = storage.size();
                for (WindSample sample : storage.values()) {
                    ewSum += sample.ewVector;
                    nsSum += sample.nsVector;
                    totalSpeed += sample.speed;
                    if (sample.speed > maxSpeed) {
                        maxSpeed = sample.speed;
                    }
                }
            }

            WindStats stats = new WindStats();

            stats.averageDirection = (int) Math.toDegrees(Math.atan2(ewSum, nsSum));
            if (stats.averageDirection < 0) {
                stats.averageDirection += 360;
            }

            stats.averageSpeed = new BigDecimal(size > 0 ? totalSpeed / size : 0).setScale(3, RoundingMode.HALF_DOWN);

            stats.maxSpeed = new BigDecimal(maxSpeed).setScale(3, RoundingMode.HALF_DOWN);

            return stats;
        }
    }

    private synchronized void startTimeoutCheck() {
        Runnable pollingRunnable = () -> {
            String detail;
            if (lastData == null) {
                detail = "No data received";
            } else {
                detail = "No data received since " + lastData.toString();
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
        };

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        // Scheduling a job on each hour to update the last hour rainfall
        offlineTimerJob = scheduler.schedule(pollingRunnable, 90, TimeUnit.SECONDS);
    }
}
