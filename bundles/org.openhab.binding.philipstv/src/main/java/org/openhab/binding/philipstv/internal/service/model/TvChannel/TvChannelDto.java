package org.openhab.binding.philipstv.internal.service.model.TvChannel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link TvChannelDto} class defines the Data Transfer Object
 * for the Philips TV API /activities/tv endpoint to get and switch tv channels.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class TvChannelDto {

    @JsonProperty
    private ChannelDto channel;

    @JsonProperty
    private ChannelListDto channelList;

    public ChannelDto getChannel() {
        return channel;
    }

    public ChannelListDto getChannelList() {
        return channelList;
    }

    public void setChannel(ChannelDto channelDto) {
        this.channel = channelDto;
    }

    public void setChannelList(ChannelListDto channelList) {
        this.channelList = channelList;
    }
}
