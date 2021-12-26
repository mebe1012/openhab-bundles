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

import java.util.Date;
import java.util.List;

import org.openhab.binding.gardena.internal.GardenaSmartCommandName;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena property.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Property {

    private String name;
    private PropertyValue value;
    private Date timestamp;
    private String unit;
    private boolean writeable;

    @SerializedName("supported_values")
    private List<String> supportedValues;
    private transient Ability ability;

    public Property() {
    }

    public Property(GardenaSmartCommandName commandName, String value) {
        this.name = commandName.toString().toLowerCase();
        this.value = new PropertyValue(value);
    }

    /**
     * Returns the name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     */
    public String getValueAsString() {
        return value != null ? value.getValue() : null;
    }

    /**
     * Returns the value of the property.
     */
    public PropertyValue getValue() {
        return value;
    }

    /**
     * Sets the value of the property.
     */
    public void setValue(PropertyValue value) {
        this.value = value;
    }

    /**
     * Returns the timestamp of the property.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the unit of the property.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns true, if the property is writeable.
     */
    public boolean isWriteable() {
        return writeable;
    }

    /**
     * Returns a list of supported values.
     */
    public List<String> getSupportedValues() {
        return supportedValues;
    }

    /**
     * Returns the ability of the property.
     */

    public Ability getAbility() {
        return ability;
    }

    /**
     * Sets the ability of the property.
     */
    public void setAbility(Ability ability) {
        this.ability = ability;
    }
}
