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
package org.openhab.binding.synopanalyzer.internal.synop;

/**
 * The {@link WindDirections} enum possible wind directions
 *
 * @author Gaël L'hopital - Initial contribution
 */
public enum WindDirections {
    N,
    NNE,
    NE,
    ENE,
    E,
    ESE,
    SE,
    SSE,
    S,
    SSW,
    SW,
    WSW,
    W,
    WNW,
    NW,
    NNW;

    /**
     * Returns the wind direction based on degree.
     */
    public static WindDirections getWindDirection(int degree) {
        double step = 360.0 / WindDirections.values().length;
        double b = Math.floor((degree + (step / 2.0)) / step);
        return WindDirections.values()[(int) (b % WindDirections.values().length)];
    }
}
