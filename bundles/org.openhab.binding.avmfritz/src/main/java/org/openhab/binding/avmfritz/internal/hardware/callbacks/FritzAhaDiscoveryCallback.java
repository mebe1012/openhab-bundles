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
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import static org.eclipse.jetty.http.HttpMethod.GET;

import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceListModel;
import org.openhab.binding.avmfritz.internal.discovery.AVMFritzDiscoveryService;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback for discovering SmartHome devices connected to a FRITZ!Box
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 */
public class FritzAhaDiscoveryCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaDiscoveryCallback.class);

    /**
     * Handler to update
     */
    private AVMFritzDiscoveryService service;

    /**
     * Constructor
     *
     * @param webIface Webinterface to FRITZ!Box
     * @param service Discovery service to call with result.
     */
    public FritzAhaDiscoveryCallback(FritzAhaWebInterface webIface, AVMFritzDiscoveryService service) {
        super(WEBSERVICE_PATH, "switchcmd=getdevicelistinfos", webIface, GET, 1);
        this.service = service;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received discovery callback response: {}", response);
        if (isValidRequest()) {
            try {
                Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
                DeviceListModel model = (DeviceListModel) u.unmarshal(new StringReader(response));
                if (model != null) {
                    for (AVMFritzBaseModel device : model.getDevicelist()) {
                        service.onDeviceAddedInternal(device);
                    }
                } else {
                    logger.debug("no model in response");
                }
            } catch (JAXBException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
            }
        } else {
            logger.debug("request is invalid: {}", status);
        }
    }
}
