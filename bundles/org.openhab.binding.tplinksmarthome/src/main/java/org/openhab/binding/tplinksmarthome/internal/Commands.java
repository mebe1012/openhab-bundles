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
package org.openhab.binding.tplinksmarthome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.model.GetRealtime;
import org.openhab.binding.tplinksmarthome.internal.model.GetSysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.GsonUtil;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.Realtime;
import org.openhab.binding.tplinksmarthome.internal.model.SetBrightness;
import org.openhab.binding.tplinksmarthome.internal.model.SetLedOff;
import org.openhab.binding.tplinksmarthome.internal.model.SetRelayState;
import org.openhab.binding.tplinksmarthome.internal.model.SetSwitchState;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightOnOff;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateBrightness;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateColor;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateColorTemperature;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightStateResponse;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.Gson;

/**
 * Class to construct the tp-link json commands and convert retrieved results into data objects.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Rewritten to use gson to parse json
 */
@NonNullByDefault
public class Commands {

    private static final String CONTEXT = "{\"context\":{\"child_ids\":[\"%s\"]},";
    private static final String SYSTEM_GET_SYSINFO = "\"system\":{\"get_sysinfo\":{}}";
    private static final String GET_SYSINFO = "{" + SYSTEM_GET_SYSINFO + "}";
    private static final String REALTIME = "\"emeter\":{\"get_realtime\":{}}";
    private static final String GET_REALTIME_AND_SYSINFO = "{" + SYSTEM_GET_SYSINFO + ", " + REALTIME + "}";
    private static final String GET_REALTIME_BULB_AND_SYSINFO = "{" + SYSTEM_GET_SYSINFO
            + ", \"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}";

    private final Gson gson = GsonUtil.createGson();
    private final Gson gsonWithExpose = GsonUtil.createGsonWithExpose();

    /**
     * Returns the json to get the energy and sys info data from the device.
     *
     * @return The json string of the command to send to the device
     */
    public static String getRealtimeAndSysinfo() {
        return GET_REALTIME_AND_SYSINFO;
    }

    /**
     * Returns the json to get the energy and sys info data from the bulb.
     *
     * @return The json string of the command to send to the bulb
     */
    public static String getRealtimeBulbAndSysinfo() {
        return GET_REALTIME_BULB_AND_SYSINFO;
    }

    /**
     * Returns the json to get the energy and sys info data from an outlet device.
     *
     * @param id optional id of the device
     * @return The json string of the command to send to the device
     */
    public static String getRealtimeWithContext(String id) {
        return String.format(CONTEXT, id) + REALTIME + "}";
    }

    /**
     * Returns the json response of the get_realtime command to the data object.
     *
     * @param realtimeResponse the json string
     * @return The data object containing the energy data from the json string
     */
    @SuppressWarnings("null")
    public Realtime getRealtimeResponse(String realtimeResponse) {
        GetRealtime getRealtime = gson.fromJson(realtimeResponse, GetRealtime.class);
        return getRealtime == null ? new Realtime() : getRealtime.getRealtime();
    }

    /**
     * Returns the json to get the sys info data from the device.
     *
     * @return The json string of the command to send to the device
     */
    public static String getSysinfo() {
        return GET_SYSINFO;
    }

    /**
     * Returns the json response of the get_sysinfo command to the data object.
     *
     * @param getSysinfoReponse the json string
     * @return The data object containing the state data from the json string
     */
    @SuppressWarnings("null")
    public Sysinfo getSysinfoReponse(String getSysinfoReponse) {
        GetSysinfo getSysinfo = gson.fromJson(getSysinfoReponse, GetSysinfo.class);
        return getSysinfo == null ? new Sysinfo() : getSysinfo.getSysinfo();
    }

    /**
     * Returns the json for the set_relay_state command to switch on or off.
     *
     * @param onOff the switch state to set
     * @param childId optional child id if multiple children are supported by a single device
     * @return The json string of the command to send to the device
     */
    public String setRelayState(OnOffType onOff, @Nullable String childId) {
        SetRelayState relayState = new SetRelayState();
        relayState.setRelayState(onOff);
        if (childId != null) {
            relayState.setChildId(childId);
        }
        return gsonWithExpose.toJson(relayState);
    }

    /**
     * Returns the json response of the set_relay_state command to the data object.
     *
     * @param relayStateResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable SetRelayState setRelayStateResponse(String relayStateResponse) {
        return gsonWithExpose.fromJson(relayStateResponse, SetRelayState.class);
    }

    /**
     * Returns the json for the set_switch_state command to switch a dimmer on or off.
     *
     * @param onOff the switch state to set
     * @return The json string of the command to send to the device
     */
    public String setSwitchState(OnOffType onOff) {
        SetSwitchState switchState = new SetSwitchState();
        switchState.setSwitchState(onOff);
        return gsonWithExpose.toJson(switchState);
    }

    /**
     * Returns the json response of the set_switch_state command to the data object.
     *
     * @param switchStateResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable SetSwitchState setSwitchStateResponse(String switchStateResponse) {
        return gsonWithExpose.fromJson(switchStateResponse, SetSwitchState.class);
    }

    /**
     * Returns the json for the set_brightness command to set the brightness value.
     *
     * @param brightness the brightness value to set
     * @return The json string of the command to send to the device
     */
    public String setDimmerBrightness(int brightness) {
        SetBrightness setBrightness = new SetBrightness();
        setBrightness.setBrightness(brightness);
        return gsonWithExpose.toJson(setBrightness);
    }

    /**
     * Returns the json response of the set_brightness command to the data object.
     *
     * @param dimmerBrightnessResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable HasErrorResponse setDimmerBrightnessResponse(String dimmerBrightnessResponse) {
        return gsonWithExpose.fromJson(dimmerBrightnessResponse, SetBrightness.class);
    }

    /**
     * Returns the json for the set_light_state command to switch a bulb on or off.
     *
     * @param onOff the switch state to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setLightState(OnOffType onOff, int transitionPeriod) {
        TransitionLightState transitionLightState = new TransitionLightState();
        LightOnOff lightState = new LightOnOff();
        lightState.setOnOff(onOff);
        lightState.setTransitionPeriod(transitionPeriod);
        transitionLightState.setLightState(lightState);
        return gson.toJson(transitionLightState);
    }

    /**
     * Returns the json for the set_led_off command to switch the led of the device on or off.
     *
     * @param onOff the led state to set
     * @param childId optional child id if multiple children are supported by a single device
     * @return The json string of the command to send to the device
     */
    public String setLedOn(OnOffType onOff, @Nullable String childId) {
        SetLedOff sLOff = new SetLedOff();
        sLOff.setLed(onOff);
        if (childId != null) {
            sLOff.setChildId(childId);
        }
        return gsonWithExpose.toJson(sLOff);
    }

    /**
     * Returns the json response for the set_led_off command to the data object.
     *
     * @param setLedOnResponse the json string
     * @return The data object containing the data from the json string
     */
    public @Nullable SetLedOff setLedOnResponse(String setLedOnResponse) {
        return gsonWithExpose.fromJson(setLedOnResponse, SetLedOff.class);
    }

    /**
     * Returns the json for the set_light_State command to set the brightness.
     *
     * @param brightness the brightness value
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setBrightness(int brightness, int transitionPeriod) {
        TransitionLightState transitionLightState = new TransitionLightState();
        LightStateBrightness lightState = new LightStateBrightness();
        lightState.setOnOff(brightness == 0 ? OnOffType.OFF : OnOffType.ON);
        lightState.setBrightness(brightness);
        lightState.setTransitionPeriod(transitionPeriod);
        transitionLightState.setLightState(lightState);
        return gson.toJson(transitionLightState);
    }

    /**
     * Returns the json for the set_light_State command to set the color.
     *
     * @param hsb the color to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setColor(HSBType hsb, int transitionPeriod) {
        TransitionLightState transitionLightState = new TransitionLightState();
        LightStateColor lightState = new LightStateColor();
        int brightness = hsb.getBrightness().intValue();
        lightState.setOnOff(brightness == 0 ? OnOffType.OFF : OnOffType.ON);
        lightState.setBrightness(brightness);
        lightState.setHue(hsb.getHue().intValue());
        lightState.setSaturation(hsb.getSaturation().intValue());
        lightState.setTransitionPeriod(transitionPeriod);
        transitionLightState.setLightState(lightState);
        return gson.toJson(transitionLightState);
    }

    /**
     * Returns the json for the set_light_State command to set the color temperature.
     *
     * @param colorTemperature the color temperature to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setColorTemperature(int colorTemperature, int transitionPeriod) {
        TransitionLightState transitionLightState = new TransitionLightState();
        LightStateColorTemperature lightState = new LightStateColorTemperature();
        lightState.setOnOff(OnOffType.ON);
        lightState.setColorTemperature(colorTemperature);
        lightState.setTransitionPeriod(transitionPeriod);
        transitionLightState.setLightState(lightState);
        return gson.toJson(transitionLightState);
    }

    /**
     * Returns the json response for the set_light_state command.
     *
     * @param response the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable TransitionLightStateResponse setTransitionLightStateResponse(String response) {
        return gson.fromJson(response, TransitionLightStateResponse.class);
    }
}
