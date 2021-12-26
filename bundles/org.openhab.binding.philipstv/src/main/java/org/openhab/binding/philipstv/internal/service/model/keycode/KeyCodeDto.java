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
package org.openhab.binding.philipstv.internal.service.model.keycode;

import org.openhab.binding.philipstv.internal.service.KeyCode;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link KeyCodeDto} class defines the Data Transfer Object
 * for the Philips TV API /input/key endpoint for remote controller emulation.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class KeyCodeDto {

    @JsonProperty
    private KeyCode key;

    public KeyCode getKey() {
        return key;
    }

    public void setKey(KeyCode key) {
        this.key = key;
    }
}
