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
package org.openhab.binding.airvisualnode.internal.json.airvisualpro;

import java.util.List;

import org.openhab.binding.airvisualnode.internal.json.DateAndTime;
import org.openhab.binding.airvisualnode.internal.json.MeasurementsInterface;
import org.openhab.binding.airvisualnode.internal.json.NodeDataInterface;
import org.openhab.binding.airvisualnode.internal.json.airvisual.Settings;
import org.openhab.binding.airvisualnode.internal.json.airvisual.Status;

/**
 * Top level object for AirVisual Node JSON data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class ProNodeData implements NodeDataInterface {

    private DateAndTime dateAndTime;
    private List<Measurements> measurements;
    private String serialNumber;
    private Settings settings;
    private Status status;

    public ProNodeData(DateAndTime dateAndTime, List<Measurements> measurements, String serialNumber, Settings settings,
            Status status) {
        this.dateAndTime = dateAndTime;
        this.measurements = measurements;
        this.serialNumber = serialNumber;
        this.settings = settings;
        this.status = status;
    }

    public DateAndTime getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(DateAndTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public MeasurementsInterface getMeasurements() {
        return measurements.get(0);
    }

    public void setMeasurements(List<Measurements> measurements) {
        this.measurements = measurements;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
