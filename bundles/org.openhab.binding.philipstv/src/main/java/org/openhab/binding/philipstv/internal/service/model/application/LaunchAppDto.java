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
 * The {@link LaunchAppDto} class defines the Data Transfer Object
 * for the Philips TV API /activities/launch endpoint for launching TV apps and launching search for content.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class LaunchAppDto {

    @JsonProperty
    private IntentDto intent;

    public void setIntent(IntentDto intent) {
        this.intent = intent;
    }

    public IntentDto getIntent() {
        return intent;
    }

    @Override
    public String toString() {
        return "LaunchAppDto{" + "intent = '" + intent + '\'' + "}";
    }
}
