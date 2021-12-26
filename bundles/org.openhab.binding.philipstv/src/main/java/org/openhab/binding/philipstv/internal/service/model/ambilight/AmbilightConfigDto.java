/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal.service.model.ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightConfigDto} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/currentconfiguration endpoint to retrieve or set the current ambilight style.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class AmbilightConfigDto {

    @JsonProperty
    private boolean isExpert;

    @JsonProperty
    private String menuSetting;

    @JsonProperty
    private String styleName;

    @JsonProperty("colorSettings")
    private AmbilightColorSettingsDto colorSettings;

    @JsonProperty("algorithm")
    private String algorithm;

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

    public boolean isIsExpert() {
        return isExpert;
    }

    public void setIsExpert(boolean isExpert) {
        this.isExpert = isExpert;
    }

    public AmbilightColorSettingsDto getColorSettings() {
        return colorSettings;
    }

    public void setColorSettings(AmbilightColorSettingsDto colorSettings) {
        this.colorSettings = colorSettings;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return "AmbilightConfigDto{" + "isExpert = '" + isExpert + '\'' + ",menuSetting = '" + menuSetting + '\''
                + ",styleName = '" + styleName + '\'' + "}";
    }
}
