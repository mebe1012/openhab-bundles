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
package org.openhab.binding.mqtt.generic.values;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a number value.
 *
 * If min / max limits are set, values below / above are (almost) silently ignored.
 *
 * <p>
 * Accepts user updates and MQTT state updates from a DecimalType, IncreaseDecreaseType and UpDownType.
 * </p>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NumberValue extends Value {
    private final Logger logger = LoggerFactory.getLogger(NumberValue.class);
    private final @Nullable BigDecimal min;
    private final @Nullable BigDecimal max;
    private final BigDecimal step;
    private final String unit;

    public NumberValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step,
            @Nullable String unit) {
        super(CoreItemFactory.NUMBER, Stream.of(QuantityType.class, IncreaseDecreaseType.class, UpDownType.class)
                .collect(Collectors.toList()));
        this.min = min;
        this.max = max;
        this.step = step == null ? BigDecimal.ONE : step;
        this.unit = unit == null ? "" : unit;
    }

    protected boolean checkConditions(BigDecimal newValue, DecimalType oldvalue) {
        BigDecimal min = this.min;
        if (min != null && newValue.compareTo(min) == -1) {
            logger.trace("Number not accepted as it is below the configured minimum");
            return false;
        }
        BigDecimal max = this.max;
        if (max != null && newValue.compareTo(max) == 1) {
            logger.trace("Number not accepted as it is above the configured maximum");
            return false;
        }

        return true;
    }

    @Override
    public String getMQTTpublishValue(@Nullable String pattern) {
        if (state == UnDefType.UNDEF) {
            return "";
        }

        String formatPattern = pattern;
        if (formatPattern == null) {
            formatPattern = "%s";
        }

        return state.format(formatPattern);
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        DecimalType oldvalue = (state == UnDefType.UNDEF) ? new DecimalType() : (DecimalType) state;
        BigDecimal newValue = null;
        if (command instanceof DecimalType) {
            if (!checkConditions(((DecimalType) command).toBigDecimal(), oldvalue)) {
                return;
            }
            state = (DecimalType) command;
        } else if (command instanceof IncreaseDecreaseType || command instanceof UpDownType) {
            if (command == IncreaseDecreaseType.INCREASE || command == UpDownType.UP) {
                newValue = oldvalue.toBigDecimal().add(step);
            } else {
                newValue = oldvalue.toBigDecimal().subtract(step);
            }
            if (!checkConditions(newValue, oldvalue)) {
                return;
            }
            state = new DecimalType(newValue);
        } else if (command instanceof QuantityType<?>) {
            QuantityType<?> qType = (QuantityType<?>) command;

            if (qType.getUnit().isCompatible(Units.ONE)) {
                newValue = qType.toBigDecimal();
            } else {
                qType = qType.toUnit(unit);
                if (qType != null) {
                    newValue = qType.toBigDecimal();
                }
            }
            if (newValue != null) {
                if (!checkConditions(newValue, oldvalue)) {
                    return;
                }
                state = new DecimalType(newValue);
            }
        } else {
            newValue = new BigDecimal(command.toString());
            if (!checkConditions(newValue, oldvalue)) {
                return;
            }
            state = new DecimalType(newValue);
        }
    }

    @Override
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        StateDescriptionFragmentBuilder builder = super.createStateDescription(readOnly);
        BigDecimal max = this.max;
        if (max != null) {
            builder = builder.withMaximum(max);
        }
        BigDecimal min = this.min;
        if (min != null) {
            builder = builder.withMinimum(min);
        }
        builder = builder.withStep(step);
        if (this.unit.length() > 0) {
            builder = builder.withPattern("%s " + this.unit.replace("%", "%%"));
        }
        return builder;
    }
}
