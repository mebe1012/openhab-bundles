/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.config;

/**
 * The {@link PhilipsTvConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class PhilipsTvConfiguration {

    public String host;
    public Integer port;
    public Integer refreshRate;

    public String pairingCode;
    public String username;
    public String password;

}
