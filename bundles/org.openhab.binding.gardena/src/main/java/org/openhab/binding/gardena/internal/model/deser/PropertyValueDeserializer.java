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
package org.openhab.binding.gardena.internal.model.deser;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.gardena.internal.model.PropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer for Gardena complex property value type.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PropertyValueDeserializer implements JsonDeserializer<PropertyValue> {
    private final Logger logger = LoggerFactory.getLogger(PropertyValueDeserializer.class);

    private static final String PROPERTY_DURATION = "duration";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_MAC = "mac";
    private static final String PROPERTY_ISCONNECTED = "isconnected";

    @Override
    public PropertyValue deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
        if (element.isJsonObject()) {
            JsonObject jsonObj = element.getAsJsonObject();
            if (jsonObj.has(PROPERTY_DURATION)) {
                long duration = jsonObj.get(PROPERTY_DURATION).getAsLong();
                if (duration != 0) {
                    duration = Math.round(duration / 60.0);
                }
                return new PropertyValue(String.valueOf(duration));
            } else if (jsonObj.has(PROPERTY_TYPE)) {
                return new PropertyValue(jsonObj.get(PROPERTY_TYPE).getAsString());
            } else if (jsonObj.has(PROPERTY_MAC) && jsonObj.has(PROPERTY_ISCONNECTED)) {
                // ignore known gateway properties
                return new PropertyValue();
            } else {
                logger.warn("Unsupported json value object, returning empty value");
                return new PropertyValue();
            }

        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            return new PropertyValue(StringUtils.join(jsonArray.iterator(), ","));
        } else {
            return new PropertyValue(element.getAsString());
        }
    }
}
