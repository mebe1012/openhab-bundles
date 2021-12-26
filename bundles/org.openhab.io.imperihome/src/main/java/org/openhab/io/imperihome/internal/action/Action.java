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
package org.openhab.io.imperihome.internal.action;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract action, called through the API by ImperiHome clients.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public abstract class Action {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String COMMAND_SOURCE = "imperihome";

    protected final EventPublisher eventPublisher;

    protected Action(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Indicates if this action can be performed on the given Item.
     *
     * @param device
     * @param item Item to check compatibility with.
     * @return True if the Item is supported.
     */
    public abstract boolean supports(AbstractDevice device, Item item);

    /**
     * Perform this action on the given Item.
     * 
     * @param device
     * @param item Item to perform action on.
     * @param value Action parameter value.
     */
    public abstract void perform(AbstractDevice device, Item item, String value);
}
