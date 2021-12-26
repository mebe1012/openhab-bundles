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
package org.openhab.binding.gardena.internal;

/**
 * A names of all GardenaSmart commands.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum GardenaSmartCommandName {
    // mower
    PARK_UNTIL_FURTHER_NOTICE,
    PARK_UNTIL_NEXT_TIMER,
    START_OVERRIDE_TIMER,
    START_RESUME_SCHEDULE,
    DURATION_PROPERTY,

    // sensor
    MEASURE_AMBIENT_TEMPERATURE,
    MEASURE_LIGHT,
    MEASURE_SOIL_HUMIDITY,
    MEASURE_SOIL_TEMPERATURE,

    // outlet
    OUTLET_MANUAL_OVERRIDE_TIME,
    OUTLET_VALVE,

    // power
    POWER_TIMER,

    // irrigation control
    WATERING_TIMER_VALVE_1,
    WATERING_TIMER_VALVE_2,
    WATERING_TIMER_VALVE_3,
    WATERING_TIMER_VALVE_4,
    WATERING_TIMER_VALVE_5,
    WATERING_TIMER_VALVE_6,

    // pump
    PUMP_MANUAL_WATERING_TIMER
}
