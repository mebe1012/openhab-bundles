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
package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvSettingsUpdateDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class ValueDto {

    @JsonProperty("Controllable")
    private String controllable;

    @JsonProperty
    private DataDto data;

    @JsonProperty("Nodeid")
    private int nodeid;

    @JsonProperty("Available")
    private String available;

    public void setControllable(String controllable) {
        this.controllable = controllable;
    }

    public String getControllable() {
        return controllable;
    }

    public void setData(DataDto data) {
        this.data = data;
    }

    public DataDto getData() {
        return data;
    }

    public void setNodeid(int nodeid) {
        this.nodeid = nodeid;
    }

    public int getNodeid() {
        return nodeid;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "Value{" + "controllable = '" + controllable + '\'' + ",data = '" + data + '\'' + ",nodeid = '" + nodeid
                + '\'' + ",available = '" + available + '\'' + "}";
    }
}
