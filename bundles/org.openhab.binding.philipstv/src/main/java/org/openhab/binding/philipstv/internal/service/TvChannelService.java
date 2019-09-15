package org.openhab.binding.philipstv.internal.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_TV_CHANNEL;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.GET_AVAILABLE_TV_CHANNEL_LIST_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_CHANNEL_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

public class TvChannelService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Name , Entry<ccid,preset> of TV Channel
    private Map<String, String> availableTvChannels;

    private final ConnectionService connectionService = new ConnectionService();

    @Override
    public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
        try {
            synchronized (this) {
                if (isTvChannelListEmpty()) { // TODO: avoids multiple inits at startup
                    availableTvChannels = getAvailableTvChannelListFromTv();
                    handler.updateChannelStateDescription(CHANNEL_TV_CHANNEL, availableTvChannels.keySet().stream()
                            .collect(Collectors.toMap(Function.identity(), Function.identity())));
                }
            }
            if (command instanceof RefreshType) {
                // Get current tv channel name
                String tvChannelName = getCurrentTvChannel();
                handler.postUpdateChannel(CHANNEL_TV_CHANNEL, new StringType(tvChannelName));
            } else if (command instanceof StringType) {
                if (availableTvChannels.containsKey(command.toString())) {
                    switchTvChannel(command);
                } else {
                    logger.warn(
                            "The given TV Channel with Name: {} couldnt be found in the local Channel List from the TV.",
                            command);
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                logger.warn("Could not execute command for TV Channels, the TV is offline.");
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error occurred during handling of command for TV Channels: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isTvChannelListEmpty() {
        return (availableTvChannels == null) || availableTvChannels.isEmpty();
    }

    private Map<String, String> getAvailableTvChannelListFromTv() throws IOException {
        JsonArray tvChannelsJsonArray;

        String jsonContent = connectionService.doHttpsGet(GET_AVAILABLE_TV_CHANNEL_LIST_PATH);
        tvChannelsJsonArray = (JsonArray) new JsonParser().parse(jsonContent).getAsJsonObject().get("Channel");

        Map<String, String> tvChannelsMap = new ConcurrentHashMap<>();

        for (JsonElement jsonElement : tvChannelsJsonArray) {
            JsonObject tvChannel = jsonElement.getAsJsonObject();
            String name = tvChannel.get("name").getAsString();
            String ccid = tvChannel.get("ccid").getAsString();

            tvChannelsMap.put(name, ccid);
        }

        logger.debug("TV Channels added: {}", tvChannelsMap.size());
        if (logger.isTraceEnabled()) {
            tvChannelsMap.keySet().forEach(app -> logger.trace("TV Channel found: {}", app));
        }
        return tvChannelsMap;
    }

    private String getCurrentTvChannel() throws IOException {
        String jsonContent = connectionService.doHttpsGet(TV_CHANNEL_PATH);
        if ("{}".equals(jsonContent)) {
            return "NA";
        }
        JsonObject jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();
        JsonObject componentJson = jsonObject.get("ccid").getAsJsonObject();
        return componentJson.get("").getAsString();
    }

    private void switchTvChannel(Command command) throws IOException {
        // Build up app launch json in format:
        // 'activities/tv', {'channel':{'ccid':'ccid'},'channelList':{'id':'allter','version':'30'}}
        JsonObject switchTvChannel = new JsonObject();
        JsonObject channel = new JsonObject();
        channel.addProperty("ccid", availableTvChannels.get(command.toString()));

        JsonObject channelList = new JsonObject();
        channelList.addProperty("id", "allter");
        channelList.addProperty("version", "30");

        switchTvChannel.add("channel", channel);
        switchTvChannel.add("channelList", channelList);

        logger.debug("Switch TV Channel json: {}", switchTvChannel);
        connectionService.doHttpsPost(TV_CHANNEL_PATH, switchTvChannel.toString());
    }

    public void clearAvailableTvChannelList() {
        if (availableTvChannels != null) {
            availableTvChannels.clear();
        }
    }
}
