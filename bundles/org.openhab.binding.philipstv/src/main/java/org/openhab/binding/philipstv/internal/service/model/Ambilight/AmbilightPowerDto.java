package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightPowerDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /ambilight/power endpoint to retrieve or set the current power state.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightPowerDto {

    @JsonProperty
    private String power;

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
