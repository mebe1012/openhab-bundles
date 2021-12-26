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
package org.openhab.binding.toon.internal.api;

/**
 * The {@link ToonState} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonState {
    private ThermostatInfo thermostatInfo;
    private PowerUsage powerUsage;
    private GasUsage gasUsage;
    private Boolean success;
    private DeviceStatusInfo deviceStatusInfo;
    private DeviceConfigInfo deviceConfigInfo;

    public ThermostatInfo getThermostatInfo() {
        return thermostatInfo;
    }

    public void setThermostatInfo(ThermostatInfo thermostatInfo) {
        this.thermostatInfo = thermostatInfo;
    }

    public PowerUsage getPowerUsage() {
        return powerUsage;
    }

    public void setPowerUsage(PowerUsage powerUsage) {
        this.powerUsage = powerUsage;
    }

    public GasUsage getGasUsage() {
        return gasUsage;
    }

    public void setGasUsage(GasUsage gasUsage) {
        this.gasUsage = gasUsage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public DeviceStatusInfo getDeviceStatusInfo() {
        return deviceStatusInfo;
    }

    public void setDeviceStatusInfo(DeviceStatusInfo deviceStatusInfo) {
        this.deviceStatusInfo = deviceStatusInfo;
    }

    public DeviceConfigInfo getDeviceConfigInfo() {
        return deviceConfigInfo;
    }

    public void setDeviceConfigInfo(DeviceConfigInfo deviceConfigInfo) {
        this.deviceConfigInfo = deviceConfigInfo;
    }

}
