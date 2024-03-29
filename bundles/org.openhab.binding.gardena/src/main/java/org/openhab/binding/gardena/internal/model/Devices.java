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

/**
 * Represents a List of Gardena devices.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Devices {

    private List<Device> devices = new ArrayList<>();

    /**
     * Returns a list of Gardena devices.
     */
    public List<Device> getDevices() {
        return devices;
    }
}
