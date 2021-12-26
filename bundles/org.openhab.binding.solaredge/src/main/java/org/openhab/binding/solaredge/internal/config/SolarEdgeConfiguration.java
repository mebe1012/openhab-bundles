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
package org.openhab.binding.solaredge.internal.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bean holding configuration data according to bridge.xml
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SolarEdgeConfiguration {

    private @Nullable String tokenOrApiKey;
    private @Nullable String solarId;

    private boolean meterInstalled = false;
    private boolean usePrivateApi = false;

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer liveDataPollingInterval = 10;
    private Integer aggregateDataPollingInterval = 60;

    public @Nullable String getTokenOrApiKey() {
        return tokenOrApiKey;
    }

    public void setTokenOrApiKey(String tokenOrApiKey) {
        this.tokenOrApiKey = tokenOrApiKey;
    }

    public @Nullable String getSolarId() {
        return solarId;
    }

    public void setSolarId(String solarId) {
        this.solarId = solarId;
    }

    public Integer getAsyncTimeout() {
        return asyncTimeout;
    }

    public void setAsyncTimeout(Integer asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    public Integer getSyncTimeout() {
        return syncTimeout;
    }

    public void setSyncTimeout(Integer syncTimeout) {
        this.syncTimeout = syncTimeout;
    }

    public Integer getLiveDataPollingInterval() {
        return liveDataPollingInterval;
    }

    public void setLiveDataPollingInterval(Integer liveDataPollingInterval) {
        this.liveDataPollingInterval = liveDataPollingInterval;
    }

    public Integer getAggregateDataPollingInterval() {
        return aggregateDataPollingInterval;
    }

    public void setAggregateDataPollingInterval(Integer aggregateDataPollingInterval) {
        this.aggregateDataPollingInterval = aggregateDataPollingInterval;
    }

    public boolean isMeterInstalled() {
        return meterInstalled;
    }

    public void setMeterInstalled(boolean meterInstalled) {
        this.meterInstalled = meterInstalled;
    }

    public boolean isUsePrivateApi() {
        return usePrivateApi;
    }

    public void setUsePrivateApi(boolean usePrivateApi) {
        this.usePrivateApi = usePrivateApi;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tokenOrApiKey", getTokenOrApiKey()).append("solarId", getSolarId())
                .append("meterInstalled", isMeterInstalled()).append("usePrivateApi", isUsePrivateApi())
                .append("live data pollingInterval", getLiveDataPollingInterval())
                .append("aggregate data pollingInterval", getAggregateDataPollingInterval())
                .append("asyncTimeout", getAsyncTimeout()).append("syncTimeout", getSyncTimeout()).toString();
    }
}
