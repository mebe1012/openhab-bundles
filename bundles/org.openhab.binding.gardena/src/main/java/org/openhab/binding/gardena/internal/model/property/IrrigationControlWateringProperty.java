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
package org.openhab.binding.gardena.internal.model.property;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena complex property value for the irrigation control.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class IrrigationControlWateringProperty extends BaseProperty {
    private IrrigationControlWateringValue value = new IrrigationControlWateringValue();

    public IrrigationControlWateringProperty(String name, int duration, int valveId) {
        super(name);

        value.state = duration == 0 ? "idle" : "manual";
        value.duration = duration;
        value.valveId = valveId;
    }

    @Override
    public String getValue() {
        return String.valueOf(value.duration);
    }

    @SuppressWarnings("unused")
    private class IrrigationControlWateringValue {
        public String state;
        public int duration;
        @SerializedName("valve_id")
        public int valveId;
    }
}
