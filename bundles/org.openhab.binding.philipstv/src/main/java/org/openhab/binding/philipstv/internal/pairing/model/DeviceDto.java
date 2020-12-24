/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link RequestCodeDto} and {@link FinishPairingDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class DeviceDto {

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("device_os")
    private String deviceOs;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    @Override
    public String toString() {
        return "Device{" + "app_name = '" + appName + '\'' + ",device_name = '" + deviceName + '\'' + ",id = '" + id
                + '\'' + ",type = '" + type + '\'' + ",app_id = '" + appId + '\'' + ",device_os = '" + deviceOs + '\''
                + "}";
    }
}
