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
package org.openhab.binding.philipstv.internal.pairing.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link RequestCodeDto} class defines the Data Transfer Object
 * for the Philips TV API /pair/request endpoint to request a pairing code.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class RequestCodeDto {

    @JsonProperty("scope")
    private List<String> scope;

    @JsonProperty("device")
    private DeviceDto device;

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setDevice(DeviceDto device) {
        this.device = device;
    }

    public DeviceDto getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "RequestPinDto{" + "scope = '" + scope + '\'' + ",device = '" + device + '\'' + "}";
    }
}
