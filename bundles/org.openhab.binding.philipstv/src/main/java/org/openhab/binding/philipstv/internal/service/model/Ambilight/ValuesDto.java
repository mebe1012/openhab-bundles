package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link AmbilightHuePowerDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class ValuesDto {

    @JsonProperty
    private ValueDto value;

    public void setValue(ValueDto value) {
        this.value = value;
    }

    public ValueDto getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ValuesItem{" + "value = '" + value + '\'' + "}";
    }
}