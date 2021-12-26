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
package org.openhab.binding.dsmr.internal.device;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorListener;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramParser;
import org.openhab.binding.dsmr.internal.device.p1telegram.TelegramParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper listener to receive telegram data from the connector, send it to the parser and forward data or errors from
 * the parser to the DSMR Device.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Moved this code out of the DSMRPort class, fixed some issues and reduced code
 */
@NonNullByDefault
public class DSMRTelegramListener implements P1TelegramListener, DSMRConnectorListener {

    private final Logger logger = LoggerFactory.getLogger(DSMRTelegramListener.class);
    private final TelegramParser parser;

    private @NonNullByDefault({}) DSMREventListener dsmrEventListener;

    /**
     * Constructor.
     *
     * @param eventListener listener to send received errors or messages to
     */
    public DSMRTelegramListener() {
        parser = new P1TelegramParser(this);
    }

    /**
     * Constructs {@link DSMRTelegramListener} with a Smarty decryptor to first decrypt incoming messages.
     *
     * @param decryptionKey Smarty decryption key
     */
    public DSMRTelegramListener(String decryptionKey) {
        parser = new SmartyDecrypter(new P1TelegramParser(this), this, decryptionKey);
    }

    /**
     * Set the DSMR event listener.
     *
     * @param eventListener the listener to set
     */
    public void setDsmrEventListener(DSMREventListener eventListener) {
        this.dsmrEventListener = eventListener;
    }

    @Override
    public void handleData(byte[] data, int length) {
        parser.parse(data, length);
    }

    @Override
    public void handleErrorEvent(DSMRConnectorErrorEvent portEvent) {
        dsmrEventListener.handleErrorEvent(portEvent);
        parser.reset();
    }

    /**
     * Handler for cosemObjects received in a P1 telegram
     *
     * @param telegram the received telegram.
     */
    @Override
    public void telegramReceived(P1Telegram telegram) {
        final TelegramState telegramState = telegram.getTelegramState();
        final List<CosemObject> cosemObjects = telegram.getCosemObjects();

        if (logger.isTraceEnabled()) {
            logger.trace("Received {} Cosem Objects with state: '{}'", cosemObjects.size(), telegramState);
        }
        if (telegramState == TelegramState.OK || telegramState == TelegramState.INVALID_ENCRYPTION_KEY) {
            dsmrEventListener.handleTelegramReceived(telegram);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Telegram received with error state '{}': {}", telegramState,
                        cosemObjects.stream().map(CosemObject::toString).collect(Collectors.joining(",")));
            }
        }
    }

    /**
     * @param lenientMode the lenientMode to set
     */
    public void setLenientMode(boolean lenientMode) {
        parser.setLenientMode(lenientMode);
    }
}
