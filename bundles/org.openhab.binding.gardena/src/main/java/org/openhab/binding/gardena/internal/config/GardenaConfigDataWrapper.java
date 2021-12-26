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
package org.openhab.binding.gardena.internal.config;

import com.google.gson.annotations.SerializedName;

/**
 * GardenaConfgData wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class GardenaConfigDataWrapper {
    @SerializedName("attributes")
    private GardenaConfig config;

    @SerializedName("type")
    private String typeToken = "token";

    public GardenaConfigDataWrapper() {
    }

    public GardenaConfigDataWrapper(GardenaConfig config) {
        this.config = config;
    }
}
