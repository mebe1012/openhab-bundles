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

import java.util.List;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action performed on a DevScene. Sends an {@link OnOffType#ON} or {@link DecimalType} 1 to the Item.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class LaunchSceneAction extends Action {

    public LaunchSceneAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();
        return acceptedCommandTypes.contains(OnOffType.class) || acceptedCommandTypes.contains(DecimalType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        ItemCommandEvent event = null;

        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();
        if (acceptedCommandTypes.contains(OnOffType.class)) {
            event = ItemEventFactory.createCommandEvent(item.getName(), OnOffType.ON, COMMAND_SOURCE);
        } else if (acceptedCommandTypes.contains(DecimalType.class)) {
            event = ItemEventFactory.createCommandEvent(item.getName(), new DecimalType(1), COMMAND_SOURCE);
        }

        if (event != null) {
            eventPublisher.post(event);
        }
    }
}
