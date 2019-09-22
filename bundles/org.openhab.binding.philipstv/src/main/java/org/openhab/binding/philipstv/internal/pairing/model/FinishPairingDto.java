package org.openhab.binding.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link FinishPairingDto} class defines the Data Transfer Object
 * for the Philips TV API /pair/grant endpoint to finish pairing.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class FinishPairingDto {

    @JsonProperty("auth")
    private AuthDto auth;

    @JsonProperty("device")
    private DeviceDto device;

    public void setAuth(AuthDto auth) {
        this.auth = auth;
    }

    public AuthDto getAuth() {
        return auth;
    }

    public void setDevice(DeviceDto device) {
        this.device = device;
    }

    public DeviceDto getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "FinishPairingDto{" + "auth = '" + auth + '\'' + ",device = '" + device + '\'' + "}";
    }
}