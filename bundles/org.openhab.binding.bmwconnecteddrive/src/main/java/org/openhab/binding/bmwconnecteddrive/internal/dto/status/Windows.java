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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link Windows} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Windows {
    public String windowDriverFront = Constants.UNDEF;// ": "CLOSED",
    public String windowDriverRear = Constants.UNDEF;// ": "CLOSED",
    public String windowPassengerFront = Constants.UNDEF;// ": "CLOSED",
    public String windowPassengerRear = Constants.UNDEF;// ": "CLOSED",
    public String sunroof = Constants.UNDEF;// ": "CLOSED",
    public String rearWindow = Constants.UNDEF;// ": "INVALID",
}
