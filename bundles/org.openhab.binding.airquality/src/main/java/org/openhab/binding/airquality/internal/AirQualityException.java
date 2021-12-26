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
package org.openhab.binding.airquality.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception that occurred while operating the binding
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirQualityException extends Exception {
    private static final long serialVersionUID = -3398100220952729815L;
    private int statusCode = -1;

    public AirQualityException(String message, Exception e) {
        super(message, e);
    }

    public AirQualityException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public @Nullable String getMessage() {
        String message = super.getMessage();
        return message == null ? null
                : String.format("Rest call failed: statusCode=%d, message=%s", statusCode, message);
    }
}
