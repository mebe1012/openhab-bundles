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
package org.openhab.binding.mqtt;

/**
 * MQTT embedded broker constants
 *
 * @author David Graeff - Initial contribution
 */
public class Constants {
    /**
     * The broker connection client ID. You can request the embedded broker connection via the MqttService:
     *
     * <pre>
     * MqttBrokerConnection c = mqttService.getBrokerConnection(Constants.CLIENTID);
     * </pre>
     */
    public static final String CLIENTID = "embedded-mqtt-broker";

    /**
     * The broker persistent identifier used for identifying configurations.
     */
    public static final String PID = "org.openhab.core.mqttembeddedbroker";

    /**
     * The configuration key used for configuring the embedded broker port.
     */
    public static final String PORT = "port";
}
