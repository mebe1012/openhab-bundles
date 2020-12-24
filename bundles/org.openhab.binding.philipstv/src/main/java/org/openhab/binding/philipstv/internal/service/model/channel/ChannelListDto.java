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
package org.openhab.binding.philipstv.internal.service.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvChannelDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class ChannelListDto {

    @JsonProperty
    private String id;

    @JsonProperty
    private String version;

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
