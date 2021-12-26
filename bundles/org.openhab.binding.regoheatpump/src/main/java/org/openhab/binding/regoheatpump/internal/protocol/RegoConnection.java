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
package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@link RegoConnection} is responsible for creating connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
public interface RegoConnection {
    /**
     * Connect to the receiver. Return true if the connection has succeeded or if already connected.
     *
     **/
    public void connect() throws IOException;

    /**
     * Return true if this manager is connected to the AVR.
     *
     * @return
     */
    public boolean isConnected();

    /**
     * Closes the connection.
     **/
    public void close();

    /**
     * Returns an output stream for this connection.
     */
    public OutputStream outputStream() throws IOException;

    /**
     * Returns an input stream for this connection.
     */
    public InputStream inputStream() throws IOException;
}
