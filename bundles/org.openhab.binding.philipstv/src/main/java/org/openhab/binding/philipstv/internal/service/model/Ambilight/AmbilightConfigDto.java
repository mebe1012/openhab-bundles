package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightConfigDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /ambilight/currentconfiguration endpoint to retrieve or set the current ambilight style.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightConfigDto {

    @JsonProperty
    private boolean isExpert;

    @JsonProperty
    private String menuSetting;

    @JsonProperty
    private String styleName;

    public void setIsExpert(boolean isExpert) {
        this.isExpert = isExpert;
    }

    public boolean isIsExpert() {
        return isExpert;
    }

    public void setMenuSetting(String menuSetting) {
        this.menuSetting = menuSetting;
    }

    public String getMenuSetting() {
        return menuSetting;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStyleName() {
        return styleName;
    }

    @Override
    public String toString() {
        return "AmbilightConfigDto{" + "isExpert = '" + isExpert + '\'' + ",menuSetting = '" + menuSetting + '\'' +
                ",styleName = '" + styleName + '\'' + "}";
    }
}