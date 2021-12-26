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
package org.openhab.binding.hdpowerview.internal.api;

/**
 * A shade type, as returned by the HD Power View Hub.
 *
 * @author Andy Lintner - Initial contribution
 */
public enum ShadePositionKind {

    POSITION(1),
    VANE(3);

    private final int key;

    ShadePositionKind(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static ShadePositionKind get(int key) {
        if (key == 1) {
            return ShadePositionKind.POSITION;
        } else if (key == 3) {
            return ShadePositionKind.VANE;
        } else {
            return null;
        }
    }
}
