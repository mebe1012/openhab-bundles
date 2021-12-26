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
package org.openhab.binding.philipstv.internal.service.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link LaunchAppDto} and {@link LaunchAppDto}
 *
 * @author Benjamin Meyer - Initial contribution
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
        return "Intent{" + "component = '" + component + '\'' + ",action = '" + action + '\'' + ",extras = '" + extras
                + '\'' + "}";
    }
}
