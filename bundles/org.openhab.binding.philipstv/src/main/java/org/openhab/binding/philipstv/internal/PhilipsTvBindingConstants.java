/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PhilipsTvBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public final class PhilipsTvBindingConstants {

    private PhilipsTvBindingConstants() {
    }

    private static final String BINDING_ID = "philipstv";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PHILIPS_TV = new ThingTypeUID(BINDING_ID, "tv");

    // List of all Channel ids. Values have to match ids in thing-types.xml
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_KEY_CODE = "keyCode";
    public static final String CHANNEL_APP_NAME = "appName";
    public static final String CHANNEL_APP_ICON = "appIcon";
    public static final String CHANNEL_TV_CHANNEL = "tvChannel";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_SEARCH_CONTENT = "searchContent";
    public static final String CHANNEL_AMBILIGHT_POWER = "ambilightPower";
    public static final String CHANNEL_AMBILIGHT_HUE_POWER = "ambilightHuePower";
    public static final String CHANNEL_AMBILIGHT_STYLE = "ambilightStyle";
    public static final String CHANNEL_AMBILIGHT_COLOR = "ambilightColor";

    // Config Parameters
    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String HTTPS = "https";

    // Timeout values
    public static final int CONNECT_TIMEOUT = 3 * 1000;

    public static final int SOCKET_TIMEOUT = 1 * 1000;

    // Default port for jointspace v6
    public static final int DEFAULT_PORT = 1926;

    // Powerstates
    public static final String POWER_ON = "On";

    public static final String POWER_OFF = "Off";

    // REST Paths
    public static final String SLASH = "/";

    public static final String API_VERSION = "6";

    public static final String BASE_PATH = SLASH + API_VERSION + SLASH;

    public static final String VOLUME_PATH = BASE_PATH + "audio" + SLASH + "volume";

    public static final String KEY_CODE_PATH = BASE_PATH + "input" + SLASH + "key";

    public static final String TV_POWERSTATE_PATH = BASE_PATH + "powerstate";

    public static final String GET_AVAILABLE_APP_LIST_PATH = BASE_PATH + "applications";

    public static final String ACTIVITIES_BASE_PATH = BASE_PATH + "activities" + SLASH;

    public static final String GET_AVAILABLE_TV_CHANNEL_LIST_PATH =
            BASE_PATH + "channeldb" + SLASH + "tv" + SLASH + "channelLists" + SLASH + "all";

    public static final String TV_CHANNEL_PATH = ACTIVITIES_BASE_PATH + "tv";
    public static final String GET_CURRENT_APP_PATH = ACTIVITIES_BASE_PATH + "current";
    public static final String LAUNCH_APP_PATH = ACTIVITIES_BASE_PATH + "launch";

    public static final String AMBILIGHT_BASE_PATH = BASE_PATH + "ambilight" + SLASH;
    public static final String AMBILIGHT_POWERSTATE_PATH = AMBILIGHT_BASE_PATH + "power";
    public static final String AMBILIGHT_CONFIG_PATH = AMBILIGHT_BASE_PATH + "currentconfiguration";
    public static final String AMBILIGHT_MODE_PATH = AMBILIGHT_BASE_PATH + "mode";

    public static final String UPDATE_SETTINGS_PATH = BASE_PATH + "menuitems" + SLASH + "settings" + SLASH + "update";

    // Logging messages
    public static final String TV_OFFLINE_MSG = "TV is not reachable and should therefore be off.";

    public static final String TV_NOT_LISTENING_MSG = "TV does not accept commands at the moment.";
}
