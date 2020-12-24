/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal.model;

/**
 * The {@link VenstarInfoData} represents a thermostat state from the REST API.
 *
 * @author William Welliver - Initial contribution
 */
public class VenstarInfoData {
    double cooltemp;
    double heattemp;

    VenstarSystemState state;
    VenstarSystemMode mode;
    int tempunits;

    public VenstarInfoData() {
        super();
    }

    public VenstarInfoData(double cooltemp, double heattemp, VenstarSystemState state, VenstarSystemMode mode) {
        super();
        this.cooltemp = cooltemp;
        this.heattemp = heattemp;
        this.state = state;
        this.mode = mode;
    }

    public double getCooltemp() {
        return cooltemp;
    }

    public void setCooltemp(double cooltemp) {
        this.cooltemp = cooltemp;
    }

    public double getHeattemp() {
        return heattemp;
    }

    public void setHeattemp(double heattemp) {
        this.heattemp = heattemp;
    }

    public VenstarSystemState getState() {
        return state;
    }

    public void setState(VenstarSystemState state) {
        this.state = state;
    }

    public VenstarSystemMode getMode() {
        return mode;
    }

    public void setMode(VenstarSystemMode mode) {
        this.mode = mode;
    }

    public int getTempunits() {
        return tempunits;
    }

    public void setTempunits(int tempunits) {
        this.tempunits = tempunits;
    }
}
