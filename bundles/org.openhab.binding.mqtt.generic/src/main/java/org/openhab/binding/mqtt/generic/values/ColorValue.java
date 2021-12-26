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
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.NotSupportedException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a color value.
 *
 * <p>
 * Accepts user updates from a HSBType, OnOffType and StringType.
 * </p>
 * Accepts MQTT state updates as OnOffType and a
 * StringType with comma separated HSB ("h,s,b"), RGB ("r,g,b"), CIE xyY ("x,y,Y") and on, off strings.
 * On, Off strings can be customized.
 *
 * @author David Graeff - Initial contribution
 * @author Aitor Iturrioz - Add CIE xyY colors support
 */
@NonNullByDefault
public class ColorValue extends Value {
    private final Logger logger = LoggerFactory.getLogger(ColorValue.class);

    private final ColorMode colorMode;
    private final String onValue;
    private final String offValue;
    private final int onBrightness;

    /**
     * Creates a non initialized color value.
     *
     * @param colorMode The color mode: HSB, RGB or XYY.
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param onBrightness When receiving a ON command, the brightness percentage is set to this value
     */
    public ColorValue(ColorMode colorMode, @Nullable String onValue, @Nullable String offValue, int onBrightness) {
        super(CoreItemFactory.COLOR,
                Stream.of(OnOffType.class, PercentType.class, StringType.class).collect(Collectors.toList()));

        if (onBrightness > 100) {
            throw new IllegalArgumentException("Brightness parameter must be <= 100");
        }

        this.colorMode = colorMode;
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.onBrightness = onBrightness;
    }

    /**
     * Updates the color state.
     */
    @Override
    public void update(Command command) throws IllegalArgumentException {
        HSBType oldvalue = (state == UnDefType.UNDEF) ? new HSBType() : (HSBType) state;
        if (command instanceof HSBType) {
            state = (HSBType) command;
        } else if (command instanceof OnOffType) {
            OnOffType boolValue = ((OnOffType) command);
            PercentType minOn = new PercentType(Math.max(oldvalue.getBrightness().intValue(), onBrightness));
            state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(),
                    boolValue == OnOffType.ON ? minOn : new PercentType(0));
        } else if (command instanceof PercentType) {
            state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), (PercentType) command);
        } else {
            final String updatedValue = command.toString();
            if (onValue.equals(updatedValue)) {
                PercentType minOn = new PercentType(Math.max(oldvalue.getBrightness().intValue(), onBrightness));
                state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), minOn);
            } else if (offValue.equals(updatedValue)) {
                state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), new PercentType(0));
            } else {
                String[] split = updatedValue.split(",");
                if (split.length != 3) {
                    throw new IllegalArgumentException(updatedValue + " is not a valid string syntax");
                }
                switch (this.colorMode) {
                    case HSB:
                        state = new HSBType(updatedValue);
                        break;
                    case RGB:
                        state = HSBType.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                                Integer.parseInt(split[2]));
                        break;
                    case XYY:
                        HSBType temp_state = HSBType.fromXY(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
                        state = new HSBType(temp_state.getHue(), temp_state.getSaturation(), new PercentType(split[2]));
                        break;
                    default:
                        logger.warn("Non supported color mode");
                }
            }
        }
    }

    private static BigDecimal factor = new BigDecimal(2.5);

    /**
     * Converts the color state to a string.
     *
     * @return Returns the color value depending on the color mode: as a HSB/HSV string (hue,saturation,brightness ->
     *         "60,100,100"), as an RGB string (red,green,blue -> "255,255,0") or as a xyY string
     *         ("0.419321,0.505255,100.00").
     */
    @Override
    public String getMQTTpublishValue(@Nullable String pattern) {
        if (state == UnDefType.UNDEF) {
            return "";
        }

        String formatPattern = pattern;
        if (formatPattern == null || "%s".equals(formatPattern)) {
            if (this.colorMode == ColorMode.XYY) {
                formatPattern = "%1$f,%2$f,%3$.2f";
            } else {
                formatPattern = "%1$d,%2$d,%3$d";
            }
        }

        HSBType hsb_state = (HSBType) state;

        switch (this.colorMode) {
            case HSB:
                return String.format(formatPattern, hsb_state.getHue().intValue(), hsb_state.getSaturation().intValue(),
                        hsb_state.getBrightness().intValue());
            case RGB:
                PercentType[] rgb = hsb_state.toRGB();
                return String.format(formatPattern, rgb[0].toBigDecimal().multiply(factor).intValue(),
                        rgb[1].toBigDecimal().multiply(factor).intValue(),
                        rgb[2].toBigDecimal().multiply(factor).intValue());
            case XYY:
                PercentType[] xyY = hsb_state.toXY();
                return String.format(Locale.ROOT, formatPattern, xyY[0].floatValue() / 100.0f,
                        xyY[1].floatValue() / 100.0f, hsb_state.getBrightness().floatValue());
            default:
                throw new NotSupportedException(String.format("Non supported color mode: {}", this.colorMode));
        }
    }
}
