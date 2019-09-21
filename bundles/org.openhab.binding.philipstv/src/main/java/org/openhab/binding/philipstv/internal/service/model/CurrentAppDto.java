package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link LaunchAppDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /activities/current endpoint for retrieving the current running TV app.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class CurrentAppDto {

    @JsonProperty
    private ComponentDto component;

    public void setComponent(ComponentDto component) {
        this.component = component;
    }

    public ComponentDto getComponent() {
        return component;
    }

    @Override
    public String toString() {
        return "Intent{" + "component = '" + component + "}";
    }
}