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
package org.openhab.binding.philipstv.internal.config;

/**
 * The {@link PhilipsTvConfiguration} class contains fields for mapping thing configuration parameters.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class PhilipsTvConfiguration {

    public String host;
    public String macAddress;
    public Integer port;
    public Integer refreshRate;
    public boolean useUpnpDiscovery = true;

    public String pairingCode;
    public String username;
    public String password;
}
