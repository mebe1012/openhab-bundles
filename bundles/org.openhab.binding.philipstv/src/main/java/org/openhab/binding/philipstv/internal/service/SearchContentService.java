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
package org.openhab.binding.philipstv.internal.service;

import static org.openhab.binding.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.LAUNCH_APP_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

import java.io.IOException;

import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.openhab.binding.philipstv.internal.service.model.application.ExtrasDto;
import org.openhab.binding.philipstv.internal.service.model.application.IntentDto;
import org.openhab.binding.philipstv.internal.service.model.application.LaunchAppDto;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for toggling the Google Assistant on the Philips TV
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class SearchContentService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTvHandler handler;

    private final ConnectionManager connectionManager;

    public SearchContentService(PhilipsTvHandler handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        if (command instanceof StringType) {
            try {
                searchForContentOnTv(command.toString());
            } catch (Exception e) {
                if (isTvOfflineException(e)) {
                    logger.warn("Could not search content on Philips TV: TV is offline.");
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
                } else if (isTvNotListeningException(e)) {
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            TV_NOT_LISTENING_MSG);
                } else {
                    logger.warn("Error during the launch of search content on Philips TV: {}", e.getMessage(), e);
                }
            }
        } else if (!(command instanceof RefreshType)) {
            logger.warn("Unknown command: {} for Channel {}", command, channel);
        }
    }

    private void searchForContentOnTv(String searchContent) throws IOException {
        LaunchAppDto launchAppDto = new LaunchAppDto();

        IntentDto intentDto = new IntentDto();
        intentDto.setAction("android.search.action.GLOBAL_SEARCH");

        ExtrasDto extrasDto = new ExtrasDto();
        extrasDto.setQuery(searchContent);

        intentDto.setExtras(extrasDto);
        launchAppDto.setIntent(intentDto);

        String searchContentLaunch = OBJECT_MAPPER.writeValueAsString(launchAppDto);

        logger.debug("Search Content Launch json: {}", searchContentLaunch);
        connectionManager.doHttpsPost(LAUNCH_APP_PATH, searchContentLaunch);
    }
}
