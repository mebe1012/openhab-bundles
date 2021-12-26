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
package org.openhab.binding.hue.internal;

import static org.openhab.binding.hue.internal.FullSensor.*;

/**
 * Updates the configuration of a presence sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
public class PresenceConfigUpdate extends SensorConfigUpdate {
    /**
     *
     * @param onOff
     */
    public void setLedIndication(boolean onOff) {
        commands.add(new Command(CONFIG_LED_INDICATION, onOff));
    }

    /**
     *
     * @param sensitivity
     */
    public void setSensitivity(int sensitivity) {
        commands.add(new Command(CONFIG_PRESENCE_SENSITIVITY, sensitivity));
    }
}
