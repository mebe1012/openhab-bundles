package org.openhab.binding.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link FinishPairingDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class AuthDto {

    @JsonProperty("auth_signature")
    private String authSignature;

    @JsonProperty("auth_timestamp")
    private String authTimestamp;

    @JsonProperty("pin")
    private String pin;

    @JsonProperty("auth_AppId")
    private String authAppId;

    public void setAuthSignature(String authSignature) {
        this.authSignature = authSignature;
    }

    public String getAuthSignature() {
        return authSignature;
    }

    public void setAuthTimestamp(String authTimestamp) {
        this.authTimestamp = authTimestamp;
    }

    public String getAuthTimestamp() {
        return authTimestamp;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setAuthAppId(String authAppId) {
        this.authAppId = authAppId;
    }

    public String getAuthAppId() {
        return authAppId;
    }

    @Override
    public String toString() {
        return "Auth{" + "auth_signature = '" + authSignature + '\'' + ",auth_timestamp = '" + authTimestamp + '\'' +
                ",pin = '" + pin + '\'' + ",auth_AppId = '" + authAppId + '\'' + "}";
    }
}