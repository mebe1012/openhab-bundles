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
package org.openhab.io.imperihome.internal.handler;

import javax.servlet.http.HttpServletRequest;

import org.openhab.io.imperihome.internal.model.RoomList;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;

/**
 * Rooms list request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class RoomListHandler {

    private final DeviceRegistry deviceRegistry;

    public RoomListHandler(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public RoomList handle(HttpServletRequest req) {
        RoomList response = new RoomList();
        response.setRooms(deviceRegistry.getRooms());
        return response;
    }
}
