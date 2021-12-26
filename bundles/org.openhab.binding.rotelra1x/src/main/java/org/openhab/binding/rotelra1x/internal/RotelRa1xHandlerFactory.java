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
package org.openhab.binding.rotelra1x.internal;

import static org.openhab.binding.rotelra1x.internal.RotelRa1xBindingConstants.THING_TYPE_AMP;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.rotelra1x.internal.handler.RotelRa1xHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RotelRa1xHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjørnstad - Initial contribution
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.rotelra1x")
public class RotelRa1xHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_AMP);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_AMP)) {
            return new RotelRa1xHandler(thing);
        }

        return null;
    }
}
