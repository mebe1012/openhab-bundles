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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChargeProfileTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ChargeProfileTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private static final int PROFILE_CALLBACK_NUMBER = 37;

    @Nullable
    ArgumentCaptor<ChannelUID> channelCaptor;
    @Nullable
    ArgumentCaptor<State> stateCaptor;
    @Nullable
    ThingHandlerCallback tc;
    @Nullable
    VehicleHandler cch;
    @Nullable
    List<ChannelUID> allChannels;
    @Nullable
    List<State> allStates;
    String driveTrain = Constants.EMPTY;
    boolean imperial;

    /**
     * Prepare environment for Vehicle Status Updates
     */
    public void setup(String type, boolean imperial) {
        driveTrain = type;
        this.imperial = imperial;
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        BMWConnectedDriveOptionProvider op = mock(BMWConnectedDriveOptionProvider.class);
        cch = new VehicleHandler(thing, op, type, imperial);
        tc = mock(ThingHandlerCallback.class);
        cch.setCallback(tc);
        channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        stateCaptor = ArgumentCaptor.forClass(State.class);
    }

    private boolean testProfile(String statusContent, int callbacksExpected) {
        assertNotNull(statusContent);

        cch.chargeProfileCallback.onResponse(statusContent);
        verify(tc, times(callbacksExpected)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());
        allChannels = channelCaptor.getAllValues();
        allStates = stateCaptor.getAllValues();

        assertNotNull(driveTrain);
        trace();
        return true;
    }

    private void trace() {
        for (int i = 0; i < allChannels.size(); i++) {
            logger.info("Channel {} {}", allChannels.get(i), allStates.get(i));
        }
    }

    /**
     * Channel testbinding::test:charge#profile-climate ON
     * Channel testbinding::test:charge#profile-mode IMMEDIATE_CHARGING
     * Channel testbinding::test:charge#window-start 11:00
     * Channel testbinding::test:charge#window-end 17:00
     * Channel testbinding::test:charge#timer1-departure 05:00
     * Channel testbinding::test:charge#timer1-enabled OFF
     * Channel testbinding::test:charge#timer1-days MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY
     * Channel testbinding::test:charge#timer2-departure 12:00
     * Channel testbinding::test:charge#timer2-enabled ON
     * Channel testbinding::test:charge#timer2-days SATURDAY
     * Channel testbinding::test:charge#timer3-departure 00:00
     * Channel testbinding::test:charge#timer3-enabled OFF
     * Channel testbinding::test:charge#timer3-days
     */
    @Test
    public void testChargingProfile() {
        logger.info("{}", Thread.currentThread().getStackTrace()[1].getMethodName());
        setup(VehicleType.ELECTRIC_REX.toString(), false);
        String content = FileReader.readFileInString("src/test/resources/webapi/charging-profile.json");
        testProfile(content, PROFILE_CALLBACK_NUMBER);
    }
}
