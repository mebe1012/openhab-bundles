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
package org.openhab.io.homekit.internal.accessories;

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.CARBON_DIOXIDE_DETECTED_STATE;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.accessories.CarbonDioxideSensorAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.carbondioxidesensor.CarbonDioxideDetectedEnum;
import io.github.hapjava.services.impl.CarbonDioxideSensorService;

/**
 *
 * @author Cody Cutrer - Initial contribution
 */
public class HomekitCarbonDioxideSensorImpl extends AbstractHomekitAccessoryImpl
        implements CarbonDioxideSensorAccessory {
    private final BooleanItemReader carbonDioxideDetectedReader;

    public HomekitCarbonDioxideSensorImpl(HomekitTaggedItem taggedItem,
            List<HomekitTaggedItem> mandatoryCharacteristics, HomekitAccessoryUpdater updater, HomekitSettings settings)
            throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        carbonDioxideDetectedReader = createBooleanReader(CARBON_DIOXIDE_DETECTED_STATE);
        getServices().add(new CarbonDioxideSensorService(this));
    }

    @Override
    public CompletableFuture<CarbonDioxideDetectedEnum> getCarbonDioxideDetectedState() {
        return CompletableFuture
                .completedFuture(carbonDioxideDetectedReader.getValue() ? CarbonDioxideDetectedEnum.ABNORMAL
                        : CarbonDioxideDetectedEnum.NORMAL);
    }

    @Override
    public void subscribeCarbonDioxideDetectedState(HomekitCharacteristicChangeCallback callback) {
        subscribe(CARBON_DIOXIDE_DETECTED_STATE, callback);
    }

    @Override
    public void unsubscribeCarbonDioxideDetectedState() {
        unsubscribe(CARBON_DIOXIDE_DETECTED_STATE);
    }
}
