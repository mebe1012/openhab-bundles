package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link TvSettingsCurrentDto} class defines the Data Transfer Object
 * for the POST Request to Philips TV API /menuitems/settings/current endpoint to retrieve current settings of the tv, e.g. the tv picture brightness.
 *
 * @author Benjamin Meyer - initial contribution
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