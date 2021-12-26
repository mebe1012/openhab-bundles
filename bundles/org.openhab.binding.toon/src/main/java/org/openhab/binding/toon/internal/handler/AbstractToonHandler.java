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
package org.openhab.binding.toon.internal.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.toon.internal.api.ToonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractToonHandler} is the base class for the various Toon handlers.
 *
 * @author Jorg de Jong - Initial contribution
 */
public abstract class AbstractToonHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AbstractToonHandler.class);
    protected ToonBridgeHandler bridgeHandler;

    public AbstractToonHandler(Thing thing) {
        super(thing);
    }

    protected void updateChannel(String channelName, State state) {
        Channel channel = getThing().getChannel(channelName);
        if (channel != null) {
            updateState(channel.getUID(), state);
        }
    }

    abstract void updateChannels(ToonState state);

    protected ToonBridgeHandler getToonBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Required bridge not defined for device {}.", this.getThing().getUID());
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ToonBridgeHandler) {
                this.bridgeHandler = (ToonBridgeHandler) handler;
            } else {
                logger.debug("No available bridge handler found for device {} bridge {} .", this.getThing().getUID(),
                        bridge.getUID());
                return null;
            }
        }
        return this.bridgeHandler;
    }
}
