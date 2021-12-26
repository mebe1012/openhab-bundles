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
package org.openhab.binding.philipstv.internal.service.model.channel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AvailableTvChannelsDto} class defines the Data Transfer Object
 * for the Philips TV API channeldb/tv/channelLists/all endpoint for retrieving all tv channels.
 *
 * @author Benjamin Meyer - Initial contribution
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
        return "AvailableTvChannelsDto{" + "channel = '" + channel + '\'' + ",id = '" + id + '\'' + ",medium = '"
                + medium + '\'' + ",version = '" + version + '\'' + ",listType = '" + listType + '\'' + ",operator = '"
                + operator + '\'' + ",installCountry = '" + installCountry + '\'' + "}";
    }
}
