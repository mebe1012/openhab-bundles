package org.openhab.binding.philipstv.internal.service;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.philipstv.internal.ConnectionManager;
import org.openhab.binding.philipstv.internal.handler.PhilipsTvHandler;
import org.openhab.binding.philipstv.internal.service.api.PhilipsTvService;
import org.openhab.binding.philipstv.internal.service.model.TvChannel.AvailableTvChannelsDto;
import org.openhab.binding.philipstv.internal.service.model.TvChannel.ChannelDto;
import org.openhab.binding.philipstv.internal.service.model.TvChannel.ChannelListDto;
import org.openhab.binding.philipstv.internal.service.model.TvChannel.TvChannelDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_TV_CHANNEL;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.GET_AVAILABLE_TV_CHANNEL_LIST_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_CHANNEL_PATH;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

public class TvChannelService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Name , ccid of TV Channel
    private Map<String, String> availableTvChannels;

    private final PhilipsTvHandler handler;

    private final ConnectionManager connectionManager;

    public TvChannelService(PhilipsTvHandler handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
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
                            "The given TV Channel with Name: {} couldn't be found in the local Channel List from the TV.",
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
        AvailableTvChannelsDto availableTvChannelsDto = OBJECT_MAPPER.readValue(
                connectionManager.doHttpsGet(GET_AVAILABLE_TV_CHANNEL_LIST_PATH), AvailableTvChannelsDto.class);

        ConcurrentMap<String, String> tvChannelsMap = availableTvChannelsDto.getChannel().stream().collect(
                Collectors.toConcurrentMap(ChannelDto::getName, ChannelDto::getCcid, (c1, c2) -> c1));

        logger.debug("TV Channels added: {}", tvChannelsMap.size());
        if (logger.isTraceEnabled()) {
            tvChannelsMap.keySet().forEach(app -> logger.trace("TV Channel found: {}", app));
        }
        return tvChannelsMap;
    }

    private String getCurrentTvChannel() throws IOException {
        TvChannelDto tvChannelDto = OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_CHANNEL_PATH),
                TvChannelDto.class);
        return Optional.ofNullable(tvChannelDto.getChannel()).map(ChannelDto::getCcid).map(availableTvChannels::get)
                .orElse("NA");
    }

    private void switchTvChannel(Command command) throws IOException {
        TvChannelDto tvChannelDto = new TvChannelDto();

        ChannelDto channelDto = new ChannelDto();
        channelDto.setCcid(availableTvChannels.get(command.toString()));
        tvChannelDto.setChannel(channelDto);

        ChannelListDto channelListDto = new ChannelListDto();
        channelListDto.setId("allter");
        channelListDto.setVersion("30");
        tvChannelDto.setChannelList(channelListDto);

        String switchTvChannelJson = OBJECT_MAPPER.writeValueAsString(tvChannelDto);
        logger.debug("Switch TV Channel json: {}", switchTvChannelJson);
        connectionManager.doHttpsPost(TV_CHANNEL_PATH, switchTvChannelJson);
    }

    public void clearAvailableTvChannelList() {
        if (availableTvChannels != null) {
            availableTvChannels.clear();
        }
    }
}
