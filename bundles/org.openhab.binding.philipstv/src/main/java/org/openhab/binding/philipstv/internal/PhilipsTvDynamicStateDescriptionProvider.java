package org.openhab.binding.philipstv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Benjamin Meyer - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, PhilipsTvDynamicStateDescriptionProvider.class },
           immediate = true)
@NonNullByDefault
public class PhilipsTvDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        List<StateOption> options = channelOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }

        if (original != null) {
            return StateDescriptionFragmentBuilder.create(original).withOptions(options).build().toStateDescription();
        }

        return StateDescriptionFragmentBuilder.create().withOptions(options).build().toStateDescription();
    }

    @Deactivate
    public void deactivate() {
        channelOptionsMap.clear();
    }
}
