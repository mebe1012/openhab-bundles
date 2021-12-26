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
package org.openhab.binding.bmwconnecteddrive.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link ConnectedDriveConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveConfiguration {

    /**
     * Depending on the location the correct server needs to be called
     */
    public String region = Constants.EMPTY;

    /**
     * BMW Connected Drive Username
     */
    public String userName = Constants.EMPTY;

    /**
     * BMW Connected Drive Password
     */
    public String password = Constants.EMPTY;

    /**
     * Prefer MyBMW API instead of BMW Connected Drive
     */
    public boolean preferMyBmw = false;
}
