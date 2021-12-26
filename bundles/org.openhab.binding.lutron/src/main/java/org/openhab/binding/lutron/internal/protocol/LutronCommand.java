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
package org.openhab.binding.lutron.internal.protocol;

/**
 * Command to a Lutron integration access point.
 *
 * @author Allan Tong - Initial contribution
 *
 */
public class LutronCommand {
    private final LutronOperation operation;
    private final LutronCommandType type;
    private final int integrationId;
    private final Object[] parameters;

    public LutronCommand(LutronOperation operation, LutronCommandType type, int integrationId, Object... parameters) {
        this.operation = operation;
        this.type = type;
        this.integrationId = integrationId;
        this.parameters = parameters;
    }

    public LutronCommandType getType() {
        return this.type;
    }

    public int getIntegrationId() {
        return this.integrationId;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(this.operation).append(this.type);

        if (integrationId >= 0) {
            builder.append(',').append(this.integrationId);
        }

        if (parameters != null) {
            for (Object parameter : parameters) {
                builder.append(',').append(parameter);
            }
        }

        return builder.toString();
    }
}
