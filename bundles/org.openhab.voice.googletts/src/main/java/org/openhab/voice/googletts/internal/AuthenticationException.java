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
package org.openhab.voice.googletts.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, if an authentication error is given.
 *
 * @author Christoph Weitkamp - Initial contribution
 *
 */
@NonNullByDefault
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
