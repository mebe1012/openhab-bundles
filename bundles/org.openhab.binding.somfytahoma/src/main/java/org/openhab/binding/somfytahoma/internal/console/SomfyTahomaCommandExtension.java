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
package org.openhab.binding.somfytahoma.internal.console;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaActionGroup;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SomfyTahomaCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class SomfyTahomaCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SCENARIOS = "scenarios";

    private final ThingRegistry thingRegistry;

    @Activate
    public SomfyTahomaCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("somfytahoma", "Interact with the Somfy Tahoma binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            Thing thing = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                thing = thingRegistry.get(thingUID);
            } catch (IllegalArgumentException e) {
                thing = null;
            }
            ThingHandler thingHandler = null;
            SomfyTahomaBridgeHandler bridgeHandler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof SomfyTahomaBridgeHandler) {
                    bridgeHandler = (SomfyTahomaBridgeHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thingUID '" + args[0] + "'");
                printUsage(console);
            } else if (bridgeHandler == null) {
                console.println("'" + args[0] + "' is not a Somfy Tahoma bridgeUID");
                printUsage(console);
            } else if (args[1].equals(SCENARIOS)) {
                for (SomfyTahomaActionGroup actionGroup : bridgeHandler.listActionGroups()) {
                    console.println("Id is \"" + actionGroup.getOid() + "\" for the scenario \""
                            + actionGroup.getLabel() + "\"");
                }
            } else {
                printUsage(console);
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage("<bridgeUID> " + SCENARIOS, "list all the scenarios with their id"));
    }
}
