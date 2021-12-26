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
package org.openhab.binding.bmwconnecteddrive.internal.dto.compat;

/**
 * The {@link CCMMessageCompat} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CCMMessageCompat {
    public String text;// "Laden nicht möglich"
    public int id;// 804,
    public String status;// "NULL",
    public String messageType;// "CCM",
    public int unitOfLengthRemaining = -1; // "18312"
}
