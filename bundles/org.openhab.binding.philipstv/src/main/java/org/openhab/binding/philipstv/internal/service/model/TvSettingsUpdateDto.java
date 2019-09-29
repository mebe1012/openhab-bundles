package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link TvSettingsUpdateDto} class defines the Data Transfer Object
 * for the Philips TV API /menuitems/settings/update endpoint to update settings of the tv, e.g. turning on/off ambilight hue power.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class TvSettingsUpdateDto {

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
        return "TvSettingsDto{" + "values = '" + values + '\'' + "}";
    }
}