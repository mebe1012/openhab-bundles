package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Part of {@link AmbilightSupportedStylesDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightSupportedStyleDto {

    @JsonProperty("styleName")
    private String styleName;

    @JsonProperty("algorithms")
    private List<String> algorithms;

    @JsonProperty("maxSpeed")
    private int maxSpeed;

    @JsonProperty("maxTuning")
    private int maxTuning;

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setAlgorithms(List<String> algorithms) {
        this.algorithms = algorithms;
    }

    public List<String> getAlgorithms() {
        return algorithms;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxTuning(int maxTuning) {
        this.maxTuning = maxTuning;
    }

    public int getMaxTuning() {
        return maxTuning;
    }

    @Override
    public String toString() {
        return "SupportedStylesItem{" + "styleName = '" + styleName + '\'' + ",algorithms = '" + algorithms + '\'' +
                ",maxSpeed = '" + maxSpeed + '\'' + ",maxTuning = '" + maxTuning + '\'' + "}";
    }
}