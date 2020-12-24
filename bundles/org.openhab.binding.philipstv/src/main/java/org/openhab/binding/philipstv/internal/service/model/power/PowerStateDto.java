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
package org.openhab.binding.philipstv.internal.service.model.power;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.STANDBY;

/**
 * The {@link PowerStateDto} class defines the Data Transfer Object
 * for the Philips TV API /powerstate endpoint to retrieve or set the current power state.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class PowerStateDto {

    @JsonProperty("powerstate")
    private String powerState;

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    @JsonIgnore
    public boolean isPoweredOn() {
        return powerState.equalsIgnoreCase(POWER_ON);
    }

    @JsonIgnore
    public boolean isStandby() {
        return powerState.equalsIgnoreCase(STANDBY);
    }
}
