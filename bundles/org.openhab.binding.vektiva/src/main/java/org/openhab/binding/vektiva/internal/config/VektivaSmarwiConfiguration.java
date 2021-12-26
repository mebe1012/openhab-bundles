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
package org.openhab.binding.vektiva.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VektivaSmarwiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class VektivaSmarwiConfiguration {

    /**
     * Smarwi configuration parameter.
     */
    public String ip = "";
    public int refreshInterval = 30;
    public boolean useWebSockets = false;
}
