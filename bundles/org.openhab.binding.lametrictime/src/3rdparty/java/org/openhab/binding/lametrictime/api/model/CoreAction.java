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
package org.openhab.binding.lametrictime.api.model;

import org.openhab.binding.lametrictime.api.local.model.UpdateAction;

public class CoreAction
{
    private final CoreApplication app;
    private final UpdateAction action;

    protected CoreAction(CoreApplication app, UpdateAction action)
    {
        this.app = app;
        this.action = action;
    }

    public CoreApplication getApp()
    {
        return app;
    }

    public UpdateAction getAction()
    {
        return action;
    }
}
