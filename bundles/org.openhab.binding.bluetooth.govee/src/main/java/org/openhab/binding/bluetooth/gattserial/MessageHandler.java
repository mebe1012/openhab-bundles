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
package org.openhab.binding.bluetooth.gattserial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public interface MessageHandler<T extends GattMessage, R extends GattMessage> {

    /**
     *
     * @param payload
     * @return true if this handler should be removed from the handler list
     */
    public boolean handleReceivedMessage(R message);

    /**
     *
     * @param payload
     * @return true if this handler should be removed from the handler list
     */
    public boolean handleFailedMessage(T message, Throwable th);
}
