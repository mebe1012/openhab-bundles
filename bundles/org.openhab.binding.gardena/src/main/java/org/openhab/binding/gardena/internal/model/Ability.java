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

import org.openhab.binding.gardena.internal.exception.GardenaException;

/**
 * Represents a Gardena ability.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Ability {

    private String name;
    private String type;
    private transient Device device;

    private List<Property> properties = new ArrayList<>();

    /**
     * Returns the name of the ability.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the ability.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a list of properties of the ability.
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Adds a property to this ability.
     */
    public void addProperty(Property property) {
        property.setAbility(this);
        properties.add(property);
    }

    /**
     * Returns the property with the specified name.
     */
    public Property getProperty(String name) throws GardenaException {
        for (Property property : properties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        throw new GardenaException("Property '" + name + "' not found in ability '" + this.name + "'");
    }

    /**
     * Returns the device of the ability.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the name of the ability.
     */
    public void setDevice(Device device) {
        this.device = device;
    }
}
