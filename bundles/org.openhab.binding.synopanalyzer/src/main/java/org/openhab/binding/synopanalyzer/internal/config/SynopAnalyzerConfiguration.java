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
package org.openhab.binding.synopanalyzer.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.synopanalyzer.internal.handler.SynopAnalyzerHandler;

/**
 * The {@link SynopAnalyzerConfiguration} is responsible for holding configuration
 * informations needed for {@link SynopAnalyzerHandler}
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SynopAnalyzerConfiguration {
    public static final String STATION_ID = "stationId";
    public long refreshInterval = 60;
    public int stationId;
}
