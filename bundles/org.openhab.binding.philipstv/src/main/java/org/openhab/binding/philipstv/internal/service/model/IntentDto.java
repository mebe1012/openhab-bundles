package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link LaunchAppDto} and {@link LaunchAppDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class IntentDto {

    @JsonProperty
    private ComponentDto component;

    @JsonProperty
    private String action;

    @JsonProperty
    private ExtrasDto extras;

    public void setComponent(ComponentDto component) {
        this.component = component;
    }

    public ComponentDto getComponent() {
        return component;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setExtras(ExtrasDto extras) {
        this.extras = extras;
    }

    public ExtrasDto getExtras() {
        return extras;
    }

    @Override
    public String toString() {
        return "Intent{" + "component = '" + component + '\'' + ",action = '" + action + '\'' + ",extras = '" + extras +
                '\'' + "}";
    }
}