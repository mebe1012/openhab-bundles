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
package org.openhab.binding.netatmo.internal;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.discovery.NetatmoModuleDiscoveryService;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.homecoach.NAHealthyHomeCoachHandler;
import org.openhab.binding.netatmo.internal.presence.NAPresenceCameraHandler;
import org.openhab.binding.netatmo.internal.station.NAMainHandler;
import org.openhab.binding.netatmo.internal.station.NAModule1Handler;
import org.openhab.binding.netatmo.internal.station.NAModule2Handler;
import org.openhab.binding.netatmo.internal.station.NAModule3Handler;
import org.openhab.binding.netatmo.internal.station.NAModule4Handler;
import org.openhab.binding.netatmo.internal.thermostat.NAPlugHandler;
import org.openhab.binding.netatmo.internal.thermostat.NATherm1Handler;
import org.openhab.binding.netatmo.internal.webhook.WelcomeWebHookServlet;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomeCameraHandler;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomeHomeHandler;
import org.openhab.binding.netatmo.internal.welcome.NAWelcomePersonHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.netatmo")
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Map<ThingUID, ServiceRegistration<?>> webHookServiceRegs = new HashMap<>();
    private final HttpService httpService;
    private final NATherm1StateDescriptionProvider stateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final LocaleProvider localeProvider;
    private final TranslationProvider translationProvider;
    private boolean backgroundDiscovery;

    @Activate
    public NetatmoHandlerFactory(final @Reference HttpService httpService,
            final @Reference NATherm1StateDescriptionProvider stateDescriptionProvider,
            final @Reference TimeZoneProvider timeZoneProvider, final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider translationProvider) {
        this.httpService = httpService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        Object property = properties.get("backgroundDiscovery");
        if (property instanceof Boolean) {
            backgroundDiscovery = ((Boolean) property).booleanValue();
        } else {
            backgroundDiscovery = false;
        }
        logger.debug("backgroundDiscovery {}", backgroundDiscovery);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(APIBRIDGE_THING_TYPE)) {
            WelcomeWebHookServlet servlet = registerWebHookServlet(thing.getUID());
            NetatmoBridgeHandler bridgeHandler = new NetatmoBridgeHandler((Bridge) thing, servlet);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(MODULE1_THING_TYPE)) {
            return new NAModule1Handler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(MODULE2_THING_TYPE)) {
            return new NAModule2Handler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(MODULE3_THING_TYPE)) {
            return new NAModule3Handler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(MODULE4_THING_TYPE)) {
            return new NAModule4Handler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(MAIN_THING_TYPE)) {
            return new NAMainHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(HOMECOACH_THING_TYPE)) {
            return new NAHealthyHomeCoachHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(PLUG_THING_TYPE)) {
            return new NAPlugHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THERM1_THING_TYPE)) {
            return new NATherm1Handler(thing, stateDescriptionProvider, timeZoneProvider);
        } else if (thingTypeUID.equals(WELCOME_HOME_THING_TYPE)) {
            return new NAWelcomeHomeHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(WELCOME_CAMERA_THING_TYPE)) {
            return new NAWelcomeCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(PRESENCE_CAMERA_THING_TYPE)) {
            return new NAPresenceCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(WELCOME_PERSON_THING_TYPE)) {
            return new NAWelcomePersonHandler(thing, timeZoneProvider);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NetatmoBridgeHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
            unregisterWebHookServlet(thingUID);
        }
    }

    private synchronized void registerDeviceDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        if (bundleContext != null) {
            NetatmoModuleDiscoveryService discoveryService = new NetatmoModuleDiscoveryService(netatmoBridgeHandler,
                    localeProvider, translationProvider);
            Map<String, Object> configProperties = new HashMap<>();
            configProperties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY,
                    Boolean.valueOf(backgroundDiscovery));
            discoveryService.activate(configProperties);
            discoveryServiceRegs.put(netatmoBridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
        }
    }

    private synchronized void unregisterDeviceDiscoveryService(ThingUID thingUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingUID);
        if (serviceReg != null) {
            NetatmoModuleDiscoveryService service = (NetatmoModuleDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }

    private synchronized @Nullable WelcomeWebHookServlet registerWebHookServlet(ThingUID thingUID) {
        WelcomeWebHookServlet servlet = null;
        if (bundleContext != null) {
            servlet = new WelcomeWebHookServlet(httpService, thingUID.getId());
            webHookServiceRegs.put(thingUID,
                    bundleContext.registerService(HttpServlet.class.getName(), servlet, new Hashtable<>()));
        }
        return servlet;
    }

    private synchronized void unregisterWebHookServlet(ThingUID thingUID) {
        ServiceRegistration<?> serviceReg = webHookServiceRegs.remove(thingUID);
        if (serviceReg != null) {
            serviceReg.unregister();
        }
    }
}
