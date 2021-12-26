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
package org.openhab.binding.sonyprojector.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SonyProjectorEthernetConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - New model configuration setting added
 */
@NonNullByDefault
public class SonyProjectorEthernetConfiguration {

    public @NonNullByDefault({}) String host;
    public @Nullable Integer port;
    public @Nullable String community;
    public @Nullable String model;
}
