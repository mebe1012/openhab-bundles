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
package org.openhab.binding.myq.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myq.internal.dto.DeviceDTO;

/**
 * The {@link MyQDeviceHandler} is responsible for handling device updates
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface MyQDeviceHandler {
    public void handleDeviceUpdate(DeviceDTO device);

    public String getSerialNumber();
}
