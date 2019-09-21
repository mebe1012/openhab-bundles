package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link AvailableAppsDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /applications endpoint for retrieving all installed apps.
 * @author Benjamin Meyer - initial contribution
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