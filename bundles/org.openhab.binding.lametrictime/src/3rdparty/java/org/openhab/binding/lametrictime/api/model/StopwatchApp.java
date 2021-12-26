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

public class StopwatchApp extends CoreApplication
{
    private static final String NAME = "com.lametric.stopwatch";

    private static final String ACTION_PAUSE = "stopwatch.pause";
    private static final String ACTION_RESET = "stopwatch.reset";
    private static final String ACTION_START = "stopwatch.start";

    public StopwatchApp()
    {
        super(NAME);
    }

    public CoreAction pause()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PAUSE));
    }

    public CoreAction reset()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_RESET));
    }

    public CoreAction start()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_START));
    }
}
