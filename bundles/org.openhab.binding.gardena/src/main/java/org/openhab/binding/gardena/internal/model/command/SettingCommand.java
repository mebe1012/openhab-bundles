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
package org.openhab.binding.gardena.internal.model.command;

import com.google.gson.annotations.SerializedName;

/**
 * Common command to set a device setting.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SettingCommand extends Command {
    private Object value;
    @SerializedName("device")
    private String deviceId;

    public SettingCommand(String name) {
        super(name);
    }

    /**
     * Returns the value of the setting command.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the setting command.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the device id of the setting command.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device id of the setting command.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
