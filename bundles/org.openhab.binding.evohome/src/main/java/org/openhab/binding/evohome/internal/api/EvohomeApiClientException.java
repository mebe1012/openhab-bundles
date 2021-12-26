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
package org.openhab.binding.evohome.internal.api;

/**
 * Exception for errors from the API Client.
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class EvohomeApiClientException extends Exception {

    public EvohomeApiClientException() {
    }

    public EvohomeApiClientException(String message) {
        super(message);
    }

    public EvohomeApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
