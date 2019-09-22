package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;

/**
 * The {@link AmbilightPowerDto} class defines the Data Transfer Object
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

    @JsonIgnore
    public boolean isPoweredOn() {
        return power.equalsIgnoreCase(POWER_ON);
    }
}
