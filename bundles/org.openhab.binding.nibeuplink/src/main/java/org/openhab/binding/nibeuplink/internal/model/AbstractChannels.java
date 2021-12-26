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
package org.openhab.binding.nibeuplink.internal.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * base class for all channellists
 *
 * @author Alexander Friese - initial contribution
 */
public abstract class AbstractChannels implements ChannelList {

    /**
     * Holds the channels
     */
    protected final Set<Channel> channels = new HashSet<>();

    /**
     * returns an unmodifiable set containing all available channels.
     *
     * @return set of unique channels
     */
    @Override
    public Set<Channel> getChannels() {
        return Collections.unmodifiableSet(channels);
    }

    /**
     * returns the matching channel, null if no match was found.
     *
     * @param channelCode the channelCode which identifies the channel
     * @return channel which belongs to the code. might be null if there is no channel found.
     */
    @Override
    public Channel fromCode(String channelCode) {
        for (Channel channel : channels) {
            if (channel.getChannelCode().equals(channelCode)) {
                return channel;
            }
        }
        return null;
    }

    /**
     * adds a channel to the internal channel list
     *
     * @param channel
     * @return
     */
    protected final <T extends Channel> T addChannel(T channel) {
        channels.add(channel);
        return channel;
    }
}
