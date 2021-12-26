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
package org.openhab.binding.sensebox.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseBoxSensor} holds a de-serialized representation
 * of the API response and the data therein...
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxSensor {

    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("unit")
    private String unit;

    @SerializedName("icon")
    private String icon;

    @SerializedName("sensorType")
    private String sensorType;

    @SerializedName("lastMeasurement")
    private SenseBoxMeasurement lastMeasurement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public SenseBoxMeasurement getLastMeasurement() {
        return lastMeasurement;
    }

    public void setLastMeasurement(SenseBoxMeasurement lastMeasurement) {
        this.lastMeasurement = lastMeasurement;
    }
}
