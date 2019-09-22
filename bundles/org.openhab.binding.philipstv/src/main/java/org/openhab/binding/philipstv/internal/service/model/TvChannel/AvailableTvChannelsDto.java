package org.openhab.binding.philipstv.internal.service.model.TvChannel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link AvailableTvChannelsDto} class defines the Data Transfer Object
 * for the Philips TV API channeldb/tv/channelLists/all endpoint for retrieving all tv channels.
 * @author Benjamin Meyer - initial contribution
 */
public class AvailableTvChannelsDto {

    @JsonProperty("Channel")
    private List<ChannelDto> channel;

    @JsonProperty
    private String id;

    @JsonProperty
    private String medium;

    @JsonProperty
    private int version;

    @JsonProperty
    private String listType;

    @JsonProperty
    private String operator;

    @JsonProperty
    private String installCountry;

    public void setChannel(List<ChannelDto> channel) {
        this.channel = channel;
    }

    public List<ChannelDto> getChannel() {
        return channel;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getMedium() {
        return medium;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public String getListType() {
        return listType;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public void setInstallCountry(String installCountry) {
        this.installCountry = installCountry;
    }

    public String getInstallCountry() {
        return installCountry;
    }

    @Override
    public String toString() {
        return "AvailableTvChannelsDto{" + "channel = '" + channel + '\'' + ",id = '" + id + '\'' + ",medium = '" +
                medium + '\'' + ",version = '" + version + '\'' + ",listType = '" + listType + '\'' + ",operator = '" +
                operator + '\'' + ",installCountry = '" + installCountry + '\'' + "}";
    }
}