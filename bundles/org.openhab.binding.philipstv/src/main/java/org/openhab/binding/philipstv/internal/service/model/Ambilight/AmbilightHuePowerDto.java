package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link AmbilightHuePowerDto} class defines the Data Transfer Object
 * for the Philips TV API /menuitems/settings/update endpoint to update the config for controlling ambilight hue power.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightHuePowerDto {

    @JsonProperty
    private List<ValuesDto> values;

    public void setValues(List<ValuesDto> values) {
        this.values = values;
    }

    public List<ValuesDto> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "AmbilightHuePowerDto{" + "values = '" + values + '\'' + "}";
    }
}