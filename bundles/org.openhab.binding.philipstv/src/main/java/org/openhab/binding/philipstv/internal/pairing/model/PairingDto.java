package org.openhab.binding.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response Data Transfer Object of {@link RequestCodeDto}
 *
 * @author Benjamin Meyer - initial contribution
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