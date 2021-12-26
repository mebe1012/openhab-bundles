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
package org.openhab.binding.gardena.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena location.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Location {

    private String id;
    private String name;
    @SerializedName("devices")
    public List<String> deviceIds = new ArrayList<>();

    /**
     * Returns the id of the location.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the location.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the device ids of the location.
     */
    public List<String> getDeviceIds() {
        return deviceIds;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Location)) {
            return false;
        }
        Location comp = (Location) obj;
        return new EqualsBuilder().append(comp.getId(), id).isEquals();
    }
}
