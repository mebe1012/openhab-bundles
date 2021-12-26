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
package org.openhab.binding.gpio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Is thrown when invalid GPIO pin Pull Up/Down resistor configuration is set
 *
 * @author Martin Dagarin - Initial contribution
 */
@NonNullByDefault
public class InvalidPullUpDownException extends Exception {
    private static final long serialVersionUID = -1281107134439928767L;
}
