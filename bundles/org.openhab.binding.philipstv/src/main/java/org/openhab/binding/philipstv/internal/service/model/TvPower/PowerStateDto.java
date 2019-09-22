package org.openhab.binding.philipstv.internal.service.model.TvPower;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.POWER_ON;

/**
 * The {@link PowerStateDto} class defines the Data Transfer Object
 * for the Philips TV API /powerstate endpoint to retrieve or set the current power state.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class PowerStateDto {

    @JsonProperty("powerstate")
    private String powerState;

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    @JsonIgnore
    public boolean isPoweredOn() {
        return powerState.equalsIgnoreCase(POWER_ON);
    }
}
