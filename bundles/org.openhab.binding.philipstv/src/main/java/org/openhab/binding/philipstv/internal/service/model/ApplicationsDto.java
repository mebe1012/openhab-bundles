package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link AvailableAppsDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class ApplicationsDto {

    @JsonProperty
    private String label;

    @JsonProperty
    private String id;

    @JsonProperty
    private String type;

    @JsonProperty
    private IntentDto intent;

    @JsonProperty
    private int order;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setIntent(IntentDto intent) {
        this.intent = intent;
    }

    public IntentDto getIntent() {
        return intent;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "ApplicationsItem{" + "label = '" + label + '\'' + ",id = '" + id + '\'' + ",type = '" + type + '\'' +
                ",intent = '" + intent + '\'' + ",order = '" + order + '\'' + "}";
    }
}