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
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Low-level interface representing a read request
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusReadRequestBlueprint extends ModbusRequestBlueprint {

    /**
     * Returns the reference of the register/coil/discrete input to to start
     * reading from with this
     * <tt>ReadMultipleRegistersRequest</tt>.
     * <p>
     *
     * @return the reference of the register
     *         to start reading from as <tt>int</tt>.
     */
    public int getReference();

    /**
     * Returns the length of the data appended
     * after the protocol header.
     * <p>
     *
     * @return the data length as <tt>int</tt>.
     */
    public int getDataLength();

    /**
     * Returns the function code of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The function code is a 1-byte non negative
     * integer value valid in the range of 0-127.<br>
     * Function codes are ordered in conformance
     * classes their values are specified in
     * <tt>net.wimpi.modbus.Modbus</tt>.
     * <p>
     *
     * @return the function code as <tt>int</tt>.
     *
     * @see net.wimpi.modbus.Modbus
     */
    public ModbusReadFunctionCode getFunctionCode();

}
