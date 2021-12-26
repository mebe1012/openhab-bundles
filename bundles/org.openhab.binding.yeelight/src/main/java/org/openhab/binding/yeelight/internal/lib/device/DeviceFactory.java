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
package org.openhab.binding.yeelight.internal.lib.device;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceFactory} creates device handler classes.
 *
 * @author Coaster Li - Initial contribution
 * @author Nikita Pogudalov - Added CeilingDeviceWithNightDevice for Ceiling 1
 */
public class DeviceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceFactory.class);

    private static final String TAG = DeviceFactory.class.getSimpleName();

    public static DeviceBase build(String model, String id) {
        DeviceType type = DeviceType.valueOf(model);
        switch (type) {
            case ceiling:
            case ceiling3:
                return new CeilingDevice(id);
            case ceiling1:
                return new CeilingDeviceWithNightDevice(id);
            case ceiling4:
                return new CeilingDeviceWithAmbientDevice(id);
            case color:
                return new WonderDevice(id);
            case mono:
                return new MonoDevice(id);
            case ct_bulb:
                return new CtBulbDevice(id);
            case stripe:
                return new PitayaDevice(id);
            case desklamp:
                return new DesklampDevice(id);
            default:
                return null;
        }
    }

    public static DeviceBase build(Map<String, String> bulbInfo) {
        DeviceBase device = build(bulbInfo.get("model"), bulbInfo.get("id"));
        if (null == device) {
            return null;
        }

        Map<String, Object> infos = new HashMap<>(bulbInfo);
        device.setBulbInfo(infos);
        LOGGER.debug("{}: DeviceFactory Device = {}", TAG, bulbInfo.get("Location"));
        // TODO enhancement!!!
        String[] addressInfo = bulbInfo.get("Location").split(":");
        device.setAddress(addressInfo[1].substring(2));
        device.setPort(Integer.parseInt(addressInfo[2]));
        device.setOnline(true);
        LOGGER.debug("{}: DeviceFactory Device info = {}, port = {}", TAG, device.getAddress(), device.getPort());
        return device;
    }
}
