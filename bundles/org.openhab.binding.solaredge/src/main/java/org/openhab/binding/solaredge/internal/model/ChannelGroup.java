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
package org.openhab.binding.solaredge.internal.model;

/**
 * Live = current values, Aggregate = aggregated data by day/month/year
 *
 * @author Alexander Friese - initial contribution
 */
public enum ChannelGroup {
    LIVE,
    AGGREGATE_DAY,
    AGGREGATE_WEEK,
    AGGREGATE_MONTH,
    AGGREGATE_YEAR
}
