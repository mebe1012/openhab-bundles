/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipstv.internal.service.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link AvailableAppsDto} class defines the Data Transfer Object
 * for the Philips TV API /applications endpoint for retrieving all installed apps.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class AvailableAppsDto {

    @JsonProperty
    private int version;

    @JsonProperty
    private List<ApplicationsDto> applications;

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setApplications(List<ApplicationsDto> applications) {
        this.applications = applications;
    }

    public List<ApplicationsDto> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "AvailableAppsDto{" + "version = '" + version + '\'' + ",applications = '" + applications + '\'' + "}";
    }
}
