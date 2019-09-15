package org.openhab.binding.philipstv.internal.service;

import com.google.gson.JsonObject;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.LAUNCH_APP_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

public class SearchContentService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionManager connectionManager;

    public SearchContentService(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
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
        // Build up search content launch json in format:
        // {"intent":{"action":"android.search.action.GLOBAL_SEARCH","extras":{"query":"Iron Man"}}}
        JsonObject searchContentLaunch = new JsonObject();
        JsonObject intent = new JsonObject();
        intent.addProperty("action", "android.search.action.GLOBAL_SEARCH");

        JsonObject extras = new JsonObject();
        extras.addProperty("query", searchContent);
        intent.add("extras", extras);
        searchContentLaunch.add("intent", intent);

        logger.debug("Search Content Launch json: {}", searchContentLaunch);
        connectionManager.doHttpsPost(LAUNCH_APP_PATH, searchContentLaunch.toString());
    }

}
