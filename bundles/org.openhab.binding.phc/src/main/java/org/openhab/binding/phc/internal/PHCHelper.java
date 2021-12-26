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
package org.openhab.binding.phc.internal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link PHCHelper} is responsible for finding the appropriate Thing(UID)
 * to the Channel of the PHC module.
 *
 * @author Jonas Hohaus - Initial contribution
 */
@NonNullByDefault
public class PHCHelper {

    /**
     * Get the ThingUID by the given parameters.
     *
     * @param thingTypeUID
     * @param moduleAddr reverse (to the reverse address - DIP switches)
     * @return
     */
    public static ThingUID getThingUIDreverse(ThingTypeUID thingTypeUID, byte moduleAddr) {
        // convert to 5-bit binary string and reverse in second step
        String thingID = StringUtils.leftPad(StringUtils.trim(Integer.toBinaryString(moduleAddr & 0xFF)), 5, '0');
        thingID = new StringBuilder(thingID).reverse().toString();

        ThingUID thingUID = new ThingUID(thingTypeUID, thingID);

        return thingUID;
    }

    /**
     * Convert the byte b into an binary String
     *
     * @param b
     * @return
     */
    public static Object bytesToBinaryString(byte[] bytes) {
        StringBuilder bin = new StringBuilder();
        for (byte b : bytes) {
            bin.append(StringUtils.leftPad(StringUtils.trim(Integer.toBinaryString(b & 0xFF)), 8, '0'));
            bin.append(' ');
        }

        return bin.toString();
    }
}
