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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command returns the Indoor and Outdoor temperature. Outdoor is not always supported.
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetIndoorOutoorTemperatures extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetIndoorOutoorTemperatures.class);

    private @Nullable QuantityType<Temperature> indoorTemperature;
    private @Nullable QuantityType<Temperature> outdoorTemperature;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        byte[] bIndoorTemperature = mm.getValues().get(0x40).getRawValue();
        byte[] bOutdoorTemperature = mm.getValues().get(0x41).getRawValue();

        if (bIndoorTemperature == null || bOutdoorTemperature == null) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect indoor or outdoor temperature");
        }

        Integer iIndoorTemperature = Integer.valueOf(bIndoorTemperature[0]);
        Integer iOutdoorTemperature = Integer.valueOf(bOutdoorTemperature[0]);

        if (iOutdoorTemperature == -1) {
            iOutdoorTemperature = null;
        } else {
            if (iOutdoorTemperature < 0) {
                iOutdoorTemperature = ((iOutdoorTemperature + 256) - 128) * -1;
            }
        }

        if (iIndoorTemperature != null) {
            indoorTemperature = new QuantityType<Temperature>(iIndoorTemperature, SIUnits.CELSIUS);
        }

        if (iOutdoorTemperature != null) {
            outdoorTemperature = new QuantityType<Temperature>(iOutdoorTemperature, SIUnits.CELSIUS);
        }

        logger.debug("Indoor Temp: {}", indoorTemperature);
        logger.debug("Outdoor Temp: {}", outdoorTemperature);

        setState(State.SUCCEEDED);
        executor.execute(() -> listener.receivedResponse(this));
    }

    public @Nullable QuantityType<Temperature> getIndoorTemperature() {
        return indoorTemperature;
    }

    public @Nullable QuantityType<Temperature> getOutdoorTemperature() {
        return outdoorTemperature;
    }

    @Override
    public int getCommandId() {
        return 272;
    }
}
