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
package org.openhab.binding.paradoxalarm.internal.model;

import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.PartitionCommandRequest;
import org.openhab.binding.paradoxalarm.internal.communication.RequestType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.CommandPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.PartitionCommand;
import org.openhab.binding.paradoxalarm.internal.handlers.Commandable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Partition} Paradox partition.
 * ID is always numeric (1-8 for Evo192)
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Partition extends Entity implements Commandable {

    private final Logger logger = LoggerFactory.getLogger(Partition.class);

    private PartitionState state = new PartitionState();

    public Partition(int id, String label) {
        super(id, label);
    }

    public PartitionState getState() {
        return state;
    }

    public Partition setState(PartitionState state) {
        this.state = state;
        logger.debug("Partition {}:\t{}", getLabel(), getState().getMainState());
        return this;
    }

    @Override
    public void handleCommand(String command) {
        PartitionCommand partitionCommand = PartitionCommand.parse(command);
        if (partitionCommand == PartitionCommand.UNKNOWN) {
            logger.debug("Command UNKNOWN will be ignored.");
            return;
        }

        logger.debug("Submitting command={} for partition=[{}]", partitionCommand, this);
        CommandPayload payload = new CommandPayload(getId(), partitionCommand);
        ParadoxIPPacket packet = new ParadoxIPPacket(payload.getBytes())
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
        PartitionCommandRequest request = new PartitionCommandRequest(RequestType.PARTITION_COMMAND, packet, null);
        IParadoxCommunicator communicator = ParadoxPanel.getInstance().getCommunicator();
        communicator.submitRequest(request);
    }
}
