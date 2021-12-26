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
package org.openhab.binding.wemo.internal.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoHttpCall} is responsible for calling a WeMo device to send commands or retrieve status updates.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class WemoHttpCall {

    private final Logger logger = LoggerFactory.getLogger(WemoHttpCall.class);

    public @Nullable String executeCall(String wemoURL, String soapHeader, String content) {
        try {
            Properties wemoHeaders = new Properties();
            wemoHeaders.setProperty("CONTENT-TYPE", WemoBindingConstants.HTTP_CALL_CONTENT_HEADER);
            wemoHeaders.put("SOAPACTION", soapHeader);

            InputStream wemoContent = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            String wemoCallResponse = HttpUtil.executeUrl("POST", wemoURL, wemoHeaders, wemoContent, null, 2000);
            return wemoCallResponse;
        } catch (IOException e) {
            // throw new IllegalStateException("Could not call WeMo", e);
            logger.debug("Could not make HTTP call to WeMo");
            return null;
        }
    }
}
