package org.openhab.binding.philipstv.internal.service.model.TvChannel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvChannelDto}
 *
 * @author Benjamin Meyer - initial contribution
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
