/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link TvSettingsUpdateDto} class defines the Data Transfer Object
 * for the Philips TV API /menuitems/settings/update endpoint to update settings of the tv, e.g. turning on/off
 * ambilight hue power.
 *
 * @author Benjamin Meyer - Initial contribution
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
