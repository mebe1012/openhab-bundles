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
package org.openhab.binding.openweathermap.internal.dto.airpollution;

import com.google.gson.annotations.SerializedName;

/**
 * Generated Plain Old Java Objects class for {@link List} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class List {
    public int dt;
    @SerializedName("main")
    public Main airQualityIndex;
    @SerializedName("components")
    public Components measurements;
}
