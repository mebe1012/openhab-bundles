/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.discovery;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.PORT;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.DEFAULT_PORT;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.HOST;
import static org.openhab.binding.philipstv.internal.PhilipsTvBindingConstants.THING_TYPE_PHILIPS_TV;

/**
 * The {@link PhilipsTvDiscoveryParticipant} is responsible for discovering Philips TV devices through UPnP.
 *
 * @author Benjamin Meyer - Initial contribution
 *
 */
public class PhilipsTvDiscoveryParticipant implements UpnpDiscoveryParticipant {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
    return Collections.singleton(THING_TYPE_PHILIPS_TV);
  }

  @Override
  public DiscoveryResult createResult(RemoteDevice device) {
    final ThingUID uid = getThingUID(device);
    if (uid == null) {
      return null;
    }

    final Map<String, Object> properties = new HashMap<>(2);
    String host = device.getIdentity().getDescriptorURL().getHost();
    properties.put(HOST, host);
    properties.put(PORT, DEFAULT_PORT);
    logger.debug("Philips TV Found: {}, using default port {}", host, DEFAULT_PORT);
    String friendlyName = device.getDetails().getFriendlyName();

    final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(THING_TYPE_PHILIPS_TV)
        .withProperties(properties).withLabel(friendlyName).build();

    return result;
  }

  @Override
  public ThingUID getThingUID(RemoteDevice device) {
    DeviceDetails details = device.getDetails();
    if (details != null) {
      ModelDetails modelDetails = details.getModelDetails();
      if (modelDetails != null) {
        String modelName = modelDetails.getModelName();
        String modelDescription = modelDetails.getModelDescription();
        if (modelName != null && modelDescription != null) {
          logger.debug("Device found: {} with desc {}", modelName, modelDescription);
          if (modelName.contains("Philips TV")) {
            logger.debug("Device found: {} with desc {}", modelName, modelDescription);
            // One Philips TV contains several UPnP devices.
            // Create unique Philips TV thing for every Media Renderer
            // device and ignore rest of the UPnP devices.
            if (modelDescription.contains("Media")) {
              // UDN shouldn't contain '-' characters.
              String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");
              logger.debug("Discovered a Philips TV '{}' model '{}' thing with UDN '{}'",
                  device.getDetails().getFriendlyName(), modelName, udn);

              return new ThingUID(THING_TYPE_PHILIPS_TV, udn);
            }
          }
        }
      }
    }
    return null;
  }

}
