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
package org.openhab.binding.sleepiq.api;

import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.sleepiq.api.impl.SleepIQImpl;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.FamilyStatus;
import org.openhab.binding.sleepiq.api.model.LoginInfo;
import org.openhab.binding.sleepiq.api.model.PauseMode;
import org.openhab.binding.sleepiq.api.model.Sleeper;

/**
 * This interface is the main API to access the SleepIQ system.
 *
 * @author Gregory Moyer
 */
public interface SleepIQ {
    /**
     * Login to the {@link Configuration configured} account. This method is not
     * required to be called before other methods because all methods must
     * ensure login before acting. However, when the only desired action is to
     * login and not retrieve other data, this method is the most efficient
     * option.
     *
     * @return basic information about the logged in user
     * @throws UnauthorizedException
     *             if the credentials provided are not valid
     * @throws LoginException
     *             if the login request fails for any reason other than bad
     *             credentials (including missing credentials)
     */
    public LoginInfo login() throws LoginException;

    /**
     * Get a list of beds connected to the account.
     *
     * @return the list of beds
     */
    public List<Bed> getBeds();

    /**
     * Get a list of people registered to this account for beds or bed positions
     * (left or right side).
     *
     * @return the list of sleepers
     */
    public List<Sleeper> getSleepers();

    /**
     * Get the status of all beds and all air chambers registered to this
     * account.
     *
     * @return the complete status of beds on the account
     */
    public FamilyStatus getFamilyStatus();

    /**
     * Get the status of "pause mode" (disabling SleepIQ data upload) for a
     * specific bed. A bed in pause mode will send no information to the SleepIQ
     * cloud services. For example, if a sleeper is in bed and disables SleepIQ
     * (enables pause mode), the service will continue to report that the bed is
     * occupied even after the sleeper exits the bed until pause mode is
     * disabled.
     *
     * @param bedId
     *            the unique identifier of the bed to query
     * @return the status of pause mode for the specified bed
     * @throws BedNotFoundException
     *             if the bed identifier was not found on the account
     */
    public PauseMode getPauseMode(String bedId) throws BedNotFoundException;

    /**
     * Create a default implementation instance of this interface. Each call to
     * this method will create a new object.
     *
     * @param config
     *            the configuration to use for the new instance
     * @return a concrete implementation of this interface
     */
    public static SleepIQ create(Configuration config, ClientBuilder clientBuilder) {
        return new SleepIQImpl(config, clientBuilder);
    }
}
