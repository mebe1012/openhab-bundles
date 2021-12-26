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
package org.openhab.binding.nibeheatpump.internal.connection;

import static org.openhab.binding.nibeheatpump.internal.NibeHeatPumpBindingConstants.*;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * The {@link ConnectorFactory} implements factory class to create Nibe connectors.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ConnectorFactory {

    public static NibeHeatPumpConnector getConnector(ThingTypeUID type) throws NibeHeatPumpException {
        if (type != null) {

            if (THING_TYPE_F1X45_UDP.equals(type) || THING_TYPE_F1X55_UDP.equals(type)
                    || THING_TYPE_F750_UDP.equals(type) || THING_TYPE_F470_UDP.equals(type)) {
                return new UDPConnector();
            } else if (THING_TYPE_F1X45_SERIAL.equals(type) || THING_TYPE_F1X55_SERIAL.equals(type)
                    || THING_TYPE_F750_SERIAL.equals(type) || THING_TYPE_F470_SERIAL.equals(type)) {
                return new SerialConnector();
            } else if (THING_TYPE_F1X45_SIMULATOR.equals(type) || THING_TYPE_F1X55_SIMULATOR.equals(type)
                    || THING_TYPE_F750_SIMULATOR.equals(type) || THING_TYPE_F470_SIMULATOR.equals(type)) {
                return new SimulatorConnector();
            }
        }

        String description = String.format("Unknown connector type %s", type);
        throw new NibeHeatPumpException(description);
    }
}
