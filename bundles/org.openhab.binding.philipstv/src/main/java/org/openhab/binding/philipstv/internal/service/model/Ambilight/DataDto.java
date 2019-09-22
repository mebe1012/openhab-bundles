package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link AmbilightHuePowerDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class DataDto {

    @JsonProperty
    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Data{" + "value = '" + value + '\'' + "}";
    }
}