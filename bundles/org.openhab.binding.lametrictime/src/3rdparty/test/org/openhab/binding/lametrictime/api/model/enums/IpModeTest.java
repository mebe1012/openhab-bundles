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
package org.openhab.binding.lametrictime.api.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class IpModeTest {
    @Test
    public void testConversion() {
        for (IpMode value : IpMode.values()) {
            assertEquals(value, IpMode.toEnum(value.toRaw()));
        }
    }

    @Test
    public void testInvalidRawValue() {
        assertNull(IpMode.toEnum("invalid raw value"));
    }

    @Test
    public void testNullRawValue() {
        assertNull(IpMode.toEnum(null));
    }
}
