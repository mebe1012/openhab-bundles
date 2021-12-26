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
package org.openhab.binding.gardena.internal.model.command;

/**
 * Command to park a mower until further notice.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class MowerParkUntilFurtherNoticeCommand extends Command {
    private static final String COMMAND = "park_until_further_notice";

    public MowerParkUntilFurtherNoticeCommand() {
        super(COMMAND);
    }
}
