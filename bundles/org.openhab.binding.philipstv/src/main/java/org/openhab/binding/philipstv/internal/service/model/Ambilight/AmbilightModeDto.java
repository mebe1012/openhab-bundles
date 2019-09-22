package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightModeDto} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/mode endpoint to retrieve or set the ambilight mode.
 * <p>
 * current (string): One of following values:
 * <p> internal: The internal ambilight algorithm is used to calculate the ambilight colours.
 * <p> manual: The cached ambilight colours are shown.
 * <p> expert: The cached ambilight colours are used as input for the internal ambilight algorithm
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightModeDto {

    @JsonProperty("current")
    private String current;

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "AmbilightModeDto{" + "current = '" + current + '\'' + "}";
    }
}