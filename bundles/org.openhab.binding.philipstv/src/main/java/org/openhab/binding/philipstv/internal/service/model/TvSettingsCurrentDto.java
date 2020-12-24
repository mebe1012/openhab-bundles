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
 * The {@link TvSettingsCurrentDto} class defines the Data Transfer Object
 * for the POST Request to Philips TV API /menuitems/settings/current endpoint to retrieve current settings of the tv,
 * e.g. the tv picture brightness.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class TvSettingsCurrentDto {

    @JsonProperty("nodes")
    private List<NodesDto> nodes;

    public void setNodes(List<NodesDto> nodes) {
        this.nodes = nodes;
    }

    public List<NodesDto> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "TvSettingsCurrentDto{" + "nodes = '" + nodes + '\'' + "}";
    }
}
