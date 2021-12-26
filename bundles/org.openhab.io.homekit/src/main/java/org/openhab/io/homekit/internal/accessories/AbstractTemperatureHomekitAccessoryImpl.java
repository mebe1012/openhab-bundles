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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.accessories.TemperatureSensor;
import com.beowulfe.hap.accessories.properties.TemperatureUnit;

/**
 *
 * @author Andy Lintner - Initial contribution
 */
abstract class AbstractTemperatureHomekitAccessoryImpl<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T>
        implements TemperatureSensor {

    private final HomekitSettings settings;

    public AbstractTemperatureHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
        this.settings = settings;
    }

    @Override
    public TemperatureUnit getTemperatureUnit() {
        return settings.useFahrenheitTemperature ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
    }

    @Override
    public double getMaximumTemperature() {
        return settings.maximumTemperature;
    }

    @Override
    public double getMinimumTemperature() {
        return settings.minimumTemperature;
    }

    protected double convertToCelsius(double degrees) {
        if (settings.useFahrenheitTemperature) {
            return Math.round((5d / 9d) * (degrees - 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }

    protected double convertFromCelsius(double degrees) {
        if (settings.useFahrenheitTemperature) {
            return Math.round((((9d / 5d) * degrees) + 32d) * 10d) / 10d;
        } else {
            return degrees;
        }
    }
}
