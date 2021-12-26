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
package org.openhab.binding.ipp.internal.discovery;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.binding.ipp.internal.IppBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * discovers ipp printers announced by mDNS
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@Component
public class IppPrinterDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(IppPrinterDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(IppBindingConstants.PRINTER_THING_TYPE);
    }

    @Override
    public String getServiceType() {
        return "_ipp._tcp.local.";
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service != null) {
            logger.trace("ServiceInfo: {}", service);
            if (service.getType() != null) {
                if (service.getType().equals(getServiceType())) {
                    String uidName = getUIDName(service);
                    return new ThingUID(IppBindingConstants.PRINTER_THING_TYPE, uidName);
                }
            }
        }
        return null;
    }

    private String getUIDName(ServiceInfo service) {
        return service.getName().replaceAll("[^A-Za-z0-9_]", "_");
    }

    private InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;
        String rp = service.getPropertyString("rp");
        if (rp == null) {
            return null;
        }
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            // remove the domain from the name
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                return null;
            }
            String inetAddress = ip.toString().substring(1); // trim leading slash

            String label = service.getName();

            int port = service.getPort();

            properties.put(IppBindingConstants.PRINTER_PARAMETER_URL, "http://" + inetAddress + ":" + port + "/" + rp);
            properties.put(IppBindingConstants.PRINTER_PARAMETER_NAME, label);

            result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
            logger.debug("Created a DiscoveryResult {} for ipp printer on host '{}' name '{}'", result,
                    properties.get(IppBindingConstants.PRINTER_PARAMETER_URL), label);
        }
        return result;
    }
}
