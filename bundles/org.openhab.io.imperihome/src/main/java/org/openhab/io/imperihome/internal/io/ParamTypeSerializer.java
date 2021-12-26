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
package org.openhab.io.imperihome.internal.io;

import java.lang.reflect.Type;

import org.openhab.io.imperihome.internal.model.param.ParamType;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for {@link ParamType}.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ParamTypeSerializer implements JsonSerializer<ParamType> {

    @Override
    public JsonElement serialize(ParamType paramType, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(paramType.getApiString());
    }
}
