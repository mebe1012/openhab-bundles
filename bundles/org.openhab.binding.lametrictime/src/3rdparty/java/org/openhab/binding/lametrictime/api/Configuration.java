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
package org.openhab.binding.lametrictime.api;

import org.openhab.binding.lametrictime.api.cloud.CloudConfiguration;
import org.openhab.binding.lametrictime.api.local.LocalConfiguration;

public class Configuration
{
    private String deviceHost;
    private String deviceApiKey;

    private boolean ignoreDeviceCertificateValidation = true;
    private boolean ignoreDeviceHostnameValidation = true;

    private boolean logging = false;
    private String logLevel = "INFO";
    private int logMax = 104857600; // 100kb

    public String getDeviceHost()
    {
        return deviceHost;
    }

    public void setDeviceHost(String deviceHost)
    {
        this.deviceHost = deviceHost;
    }

    public Configuration withDeviceHost(String deviceHost)
    {
        this.deviceHost = deviceHost;
        return this;
    }

    public String getDeviceApiKey()
    {
        return deviceApiKey;
    }

    public void setDeviceApiKey(String deviceApiKey)
    {
        this.deviceApiKey = deviceApiKey;
    }

    public Configuration withDeviceApiKey(String deviceApiKey)
    {
        this.deviceApiKey = deviceApiKey;
        return this;
    }

    public boolean isIgnoreDeviceCertificateValidation()
    {
        return ignoreDeviceCertificateValidation;
    }

    public void setIgnoreDeviceCertificateValidation(boolean ignoreDeviceCertificateValidation)
    {
        this.ignoreDeviceCertificateValidation = ignoreDeviceCertificateValidation;
    }

    public Configuration withIgnoreDeviceCertificateValidation(boolean ignoreDeviceCertificateValidation)
    {
        this.ignoreDeviceCertificateValidation = ignoreDeviceCertificateValidation;
        return this;
    }

    public boolean isIgnoreDeviceHostnameValidation()
    {
        return ignoreDeviceHostnameValidation;
    }

    public void setIgnoreDeviceHostnameValidation(boolean ignoreDeviceHostnameValidation)
    {
        this.ignoreDeviceHostnameValidation = ignoreDeviceHostnameValidation;
    }

    public Configuration withIgnoreDeviceHostnameValidation(boolean ignoreDeviceHostnameValidation)
    {
        this.ignoreDeviceHostnameValidation = ignoreDeviceHostnameValidation;
        return this;
    }

    public boolean isLogging()
    {
        return logging;
    }

    public void setLogging(boolean logging)
    {
        this.logging = logging;
    }

    public Configuration withLogging(boolean logging)
    {
        this.logging = logging;
        return this;
    }

    public String getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
    }

    public Configuration withLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogMax()
    {
        return logMax;
    }

    public void setLogMax(int logMax)
    {
        this.logMax = logMax;
    }

    public Configuration withLogMax(int logMax)
    {
        this.logMax = logMax;
        return this;
    }

    public LocalConfiguration getLocalConfig()
    {
        return new LocalConfiguration().withHost(deviceHost)
                                       .withApiKey(deviceApiKey)
                                       .withIgnoreCertificateValidation(ignoreDeviceCertificateValidation)
                                       .withIgnoreHostnameValidation(ignoreDeviceHostnameValidation)
                                       .withLogging(logging)
                                       .withLogLevel(logLevel)
                                       .withLogMax(logMax);
    }

    public CloudConfiguration getCloudConfig()
    {
        return new CloudConfiguration().withLogging(logging)
                                       .withLogLevel(logLevel)
                                       .withLogMax(logMax);
    }
}
