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
package org.openhab.binding.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response Data Transfer Object of {@link RequestCodeDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class PairingDto {

    @JsonProperty("auth_key")
    private String authKey;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("timeout")
    private String timeout;

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "PairingCodeDto{" + "auth_key = '" + authKey + '\'' + ",timestamp = '" + timestamp + '\'' + "}";
    }
}
