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
package org.openhab.binding.fronius.internal.math;

/**
 * Helper class for unit conversions
 *
 * @author Thomas Rokohl - Initial contribution
 *
 */
public class KilowattConverter {

    public static double getConvertFactor(String fromUnit, String toUnit) {
        String adjustedFromUnit = fromUnit.replace("Wh", "");
        String adjustedtoUnit = toUnit.replace("Wh", "");
        return SiPrefixFactors.getFactorToBaseUnit(adjustedFromUnit) * 1
                / SiPrefixFactors.getFactorToBaseUnit(adjustedtoUnit);
    }

    public static double convertTo(double value, String fromUnit, String toUnit) {
        return value * getConvertFactor(fromUnit, toUnit);
    }
}
