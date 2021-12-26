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
package org.openhab.binding.toon.internal;

import static org.openhab.binding.toon.internal.ToonBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.toon.internal.discovery.ToonDiscoveryService;
import org.openhab.binding.toon.internal.handler.ToonBridgeHandler;
import org.openhab.binding.toon.internal.handler.ToonDisplayHandler;
import org.openhab.binding.toon.internal.handler.ToonPlugHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ToonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jorg de Jong - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.toon")
public class ToonHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(ToonHandlerFactory.class);

    private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler for {} {}", thing, thing.getThingTypeUID());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(APIBRIDGE_THING_TYPE)) {
            ToonBridgeHandler bridgeHandler = new ToonBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(PLUG_THING_TYPE)) {
            return new ToonPlugHandler(thing);
        } else if (thingTypeUID.equals(MAIN_THING_TYPE)) {
            return new ToonDisplayHandler(thing);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    private void registerDeviceDiscoveryService(ToonBridgeHandler toonBridgeHandler) {
        ToonDiscoveryService discoveryService = new ToonDiscoveryService(toonBridgeHandler);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        logger.debug("removeHandler");
        if (discoveryServiceReg != null && thingHandler.getThing().getThingTypeUID().equals(APIBRIDGE_THING_TYPE)) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }

}
