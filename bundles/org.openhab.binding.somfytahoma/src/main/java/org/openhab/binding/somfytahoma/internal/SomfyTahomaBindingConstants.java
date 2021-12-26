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
package org.openhab.binding.somfytahoma.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SomfyTahomaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 * @author Laurent Garnier - Other portals integration
 */
@NonNullByDefault
public class SomfyTahomaBindingConstants {

    public static final String BINDING_ID = "somfytahoma";

    // Things
    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // Gateway
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");

    // Silent Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_SILENT = new ThingTypeUID(BINDING_ID,
            "rollershutter_silent");

    // Uno Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_UNO = new ThingTypeUID(BINDING_ID, "rollershutter_uno");

    // Screen
    public static final ThingTypeUID THING_TYPE_SCREEN = new ThingTypeUID(BINDING_ID, "screen");

    // Venetian Blind
    public static final ThingTypeUID THING_TYPE_VENETIANBLIND = new ThingTypeUID(BINDING_ID, "venetianblind");

    // Exterior Screen
    public static final ThingTypeUID THING_TYPE_EXTERIORSCREEN = new ThingTypeUID(BINDING_ID, "exteriorscreen");

    // Exterior Venetian Blind
    public static final ThingTypeUID THING_TYPE_EXTERIORVENETIANBLIND = new ThingTypeUID(BINDING_ID,
            "exteriorvenetianblind");

    // Garage Door
    public static final ThingTypeUID THING_TYPE_GARAGEDOOR = new ThingTypeUID(BINDING_ID, "garagedoor");

    // Awning
    public static final ThingTypeUID THING_TYPE_AWNING = new ThingTypeUID(BINDING_ID, "awning");

    // Actiongroup
    public static final ThingTypeUID THING_TYPE_ACTIONGROUP = new ThingTypeUID(BINDING_ID, "actiongroup");

    // On Off
    public static final ThingTypeUID THING_TYPE_ONOFF = new ThingTypeUID(BINDING_ID, "onoff");

    // Light
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");

    // DimmerLight
    public static final ThingTypeUID THING_TYPE_DIMMER_LIGHT = new ThingTypeUID(BINDING_ID, "dimmerlight");

    // Light sensor
    public static final ThingTypeUID THING_TYPE_LIGHTSENSOR = new ThingTypeUID(BINDING_ID, "lightsensor");

    // Smoke sensor
    public static final ThingTypeUID THING_TYPE_SMOKESENSOR = new ThingTypeUID(BINDING_ID, "smokesensor");

    // Contact sensor
    public static final ThingTypeUID THING_TYPE_CONTACTSENSOR = new ThingTypeUID(BINDING_ID, "contactsensor");

    // Occupancy sensor
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");

    // Water sensor
    public static final ThingTypeUID THING_TYPE_WATERSENSOR = new ThingTypeUID(BINDING_ID, "watersensor");

    // Humidity sensor
    public static final ThingTypeUID THING_TYPE_HUMIDITYSENSOR = new ThingTypeUID(BINDING_ID, "humiditysensor");

    // Window
    public static final ThingTypeUID THING_TYPE_WINDOW = new ThingTypeUID(BINDING_ID, "window");

    // Alarm
    public static final ThingTypeUID THING_TYPE_INTERNAL_ALARM = new ThingTypeUID(BINDING_ID, "internalalarm");
    public static final ThingTypeUID THING_TYPE_EXTERNAL_ALARM = new ThingTypeUID(BINDING_ID, "externalalarm");
    public static final ThingTypeUID THING_TYPE_MYFOX_ALARM = new ThingTypeUID(BINDING_ID, "myfoxalarm");

    // Pod
    public static final ThingTypeUID THING_TYPE_POD = new ThingTypeUID(BINDING_ID, "pod");

    // Heating system
    public static final ThingTypeUID THING_TYPE_VALVE_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID,
            "valveheatingsystem");
    public static final ThingTypeUID THING_TYPE_ZWAVE_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID, "heatingsystem");
    public static final ThingTypeUID THING_TYPE_ONOFF_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID,
            "onoffheatingsystem");
    public static final ThingTypeUID THING_TYPE_EXTERIOR_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID,
            "exteriorheatingsystem");

    // Door lock
    public static final ThingTypeUID THING_TYPE_DOOR_LOCK = new ThingTypeUID(BINDING_ID, "doorlock");

    // Pergola
    public static final ThingTypeUID THING_TYPE_PERGOLA = new ThingTypeUID(BINDING_ID, "pergola");

    // Bioclimatic Pergola
    public static final ThingTypeUID THING_TYPE_BIOCLIMATIC_PERGOLA = new ThingTypeUID(BINDING_ID,
            "bioclimaticpergola");

    // Window handle
    public static final ThingTypeUID THING_TYPE_WINDOW_HANDLE = new ThingTypeUID(BINDING_ID, "windowhandle");

    // Temperature sensor
    public static final ThingTypeUID THING_TYPE_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID, "temperaturesensor");

    // Gate
    public static final ThingTypeUID THING_TYPE_GATE = new ThingTypeUID(BINDING_ID, "gate");

    // Curtains
    public static final ThingTypeUID THING_TYPE_CURTAIN = new ThingTypeUID(BINDING_ID, "curtain");

    // Electricity sensor
    public static final ThingTypeUID THING_TYPE_ELECTRICITYSENSOR = new ThingTypeUID(BINDING_ID, "electricitysensor");

    // Dock
    public static final ThingTypeUID THING_TYPE_DOCK = new ThingTypeUID(BINDING_ID, "dock");

    // Siren
    public static final ThingTypeUID THING_TYPE_SIREN = new ThingTypeUID(BINDING_ID, "siren");

    // Adjustable slats roller shutter
    public static final ThingTypeUID THING_TYPE_ADJUSTABLE_SLATS_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID,
            "adjustableslatsrollershutter");

    // MyFox Camera
    public static final ThingTypeUID THING_TYPE_MYFOX_CAMERA = new ThingTypeUID(BINDING_ID, "myfoxcamera");

    // Thermostat
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String RSSI = "rssi";

    // Gateway
    public static final String STATUS = "status";
    public static final String SCENARIOS = "scenarios";

    // Roller shutter, Awning, Screen, Blind, Garage door, Window, Curtain
    public static final String CONTROL = "control";

    // Adjustable slats roller shutter
    public static final String ROCKER = "rocker";

    // Silent roller shutter
    public static final String CONTROL_SILENT = "control_silent";

    // Bioclimatic Pergola
    public static final String SLATS = "slats";
    public static final String PERGOLA_COMMAND = "pergola_command";

    // Blind, Bioclimatic Pergola
    public static final String ORIENTATION = "orientation";

    // Blind
    public static final String CLOSURE_AND_ORIENTATION = "closure_orientation";

    // Action group
    public static final String EXECUTE_ACTION = "execute_action";

    // OnOff, Light
    public static final String SWITCH = "switch";

    // Dimmer Light
    public static final String LIGHT_INTENSITY = "light_intensity";

    // Door lock
    public static final String LOCK = "lock";
    public static final String OPEN = "open";

    // Smoke sensor, Occupancy sensor, Contact sensor, Water sensor
    public static final String CONTACT = "contact";
    public static final String SENSOR_DEFECT = "sensor_defect";

    // Humidity sensor
    public static final String HUMIDITY = "humidity";

    // Smoke sensor
    public static final String ALARM_CHECK = "alarm_check";
    public static final String RADIO_BATTERY = "radio_battery";
    public static final String SENSOR_BATTERY = "sensor_battery";

    // Light sensor
    public static final String LUMINANCE = "luminance";

    // Temperature sensor
    public static final String TEMPERATURE = "temperature";

    // Alarm
    public static final String ALARM_COMMAND = "alarm_command";
    public static final String ALARM_STATE = "alarm_state";
    public static final String TARGET_ALARM_STATE = "target_alarm_state";
    public static final String INTRUSION_CONTROL = "intrusion_control";
    public static final String INTRUSION_STATE = "intrusion_state";

    // Pod
    public static final String CYCLIC_BUTTON = "cyclic_button";
    public static final String LIGHTING_LED_POD_MODE = "lighting_led_pod_mode";

    // Heating system
    public static final String TARGET_TEMPERATURE = "target_temperature";
    public static final String CURRENT_TEMPERATURE = "current_temperature";
    public static final String CURRENT_STATE = "current_state";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String TARGET_HEATING_LEVEL = "target_heating_level";
    public static final String HEATING_LEVEL = "heating_level";

    // Thermostat
    public static final String HEATING_MODE = "heating_mode";
    public static final String DEROGATION_ACTIVATION = "derogation_activation";

    // Thermostat & Valve Heating system
    public static final String DEROGATED_TARGET_TEMPERATURE = "derogated_target_temperature";
    public static final String DEROGATION_HEATING_MODE = "derogation_heating_mode";

    // Valve heating system
    public static final String CURRENT_HEATING_MODE = "current_heating_mode";
    public static final String OPEN_CLOSED_VALVE = "open_closed_valve";
    public static final String OPERATING_MODE = "operating_mode";

    // Window handle
    public static final String HANDLE_STATE = "handle_state";

    // Gate
    public static final String GATE_STATE = "gate_state";
    public static final String GATE_COMMAND = "gate_command";
    public static final String GATE_POSITION = "gate_position";

    // ElectricitySensor
    public static final String ENERGY_CONSUMPTION = "energy_consumption";

    // Dock
    public static final String BATTERY_STATUS = "battery_status";
    public static final String SIREN_STATUS = "siren_status";
    public static final String SHORT_BIP = "short_beep";
    public static final String LONG_BIP = "long_beep";

    // Siren
    public static final String MEMORIZED_VOLUME = "memorized_volume";
    public static final String ONOFF_STATE = "onoff";
    public static final String BATTERY = "battery";

    // Myfox Alarm
    public static final String MYFOX_ALARM_COMMAND = "myfox_alarm_command";

    // Myfox Alarm & Camera
    public static final String CLOUD_STATUS = "cloud_status";

    // Myfox Camera
    public static final String SHUTTER = "shutter";

    // Constants
    public static final String TAHOMA_PORTAL = "www.tahomalink.com";
    public static final String API_BASE_URL = "/enduser-mobile-web/enduserAPI/";
    public static final String EVENTS_URL = "events/";
    public static final String SETUP_URL = "setup/";
    public static final String GATEWAYS_URL = SETUP_URL + "gateways/";
    public static final String DEVICES_URL = SETUP_URL + "devices/";
    public static final String REFRESH_URL = DEVICES_URL + "states/refresh";
    public static final String EXEC_URL = "exec/";
    public static final String DELETE_URL = EXEC_URL + "current/setup/";
    public static final String TAHOMA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    public static final int TAHOMA_TIMEOUT = 5;
    public static final String UNAUTHORIZED = "Not logged in";
    public static final int TYPE_NONE = 0;
    public static final int TYPE_PERCENT = 1;
    public static final int TYPE_DECIMAL = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_BOOLEAN = 6;
    public static final String UNAVAILABLE = "unavailable";
    public static final String AUTHENTICATION_CHALLENGE = "HTTP protocol violation: Authentication challenge without WWW-Authenticate header";
    public static final String TOO_MANY_REQUESTS = "Too many requests, try again later";
    public static final int SUSPEND_TIME = 120;
    public static final int RECONCILIATION_TIME = 600;

    // Commands
    public static final String COMMAND_MY = "my";
    public static final String COMMAND_SET_CLOSURE = "setClosure";
    public static final String COMMAND_SET_CLOSURE_ORIENTATION = "setClosureAndOrientation";
    public static final String COMMAND_SET_DEPLOYMENT = "setDeployment";
    public static final String COMMAND_SET_ORIENTATION = "setOrientation";
    public static final String COMMAND_SET_CLOSURESPEED = "setClosureAndLinearSpeed";
    public static final String COMMAND_SET_HEATINGLEVEL = "setHeatingLevel";
    public static final String COMMAND_SET_PEDESTRIANPOSITION = "setPedestrianPosition";
    public static final String COMMAND_SET_ROCKERPOSITION = "setRockerPosition";
    public static final String COMMAND_SET_DEROGATION = "setDerogation";
    public static final String COMMAND_UP = "up";
    public static final String COMMAND_DOWN = "down";
    public static final String COMMAND_OPEN = "open";
    public static final String COMMAND_CLOSE = "close";
    public static final String COMMAND_OPEN_SLATS = "openSlats";
    public static final String COMMAND_CLOSE_SLATS = "closeSlats";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_OFF = "off";
    public static final String COMMAND_CHECK_TRIGGER = "checkEventTrigger";

    // States
    public static final String NAME_STATE = "core:NameState";
    public static final String RSSI_LEVEL_STATE = "core:RSSILevelState";
    public static final String STATUS_STATE = "core:StatusState";
    public static final String ENERGY_CONSUMPTION_STATE = "core:ElectricEnergyConsumptionState";
    public static final String CYCLIC_BUTTON_STATE = "core:CyclicButtonState";
    public static final String BATTERY_STATUS_STATE = "internal:BatteryStatusState";
    public static final String SLATE_ORIENTATION_STATE = "core:SlateOrientationState";
    public static final String CLOSURE_OR_ROCKER_STATE = "core:ClosureOrRockerPositionState";
    public static final String MYFOX_SHUTTER_STATUS_STATE = "myfox:ShutterStatusState";
    public static final String TARGET_CLOSURE_STATE = "core:TargetClosureState";
    public static final String WATER_DETECTION_STATE = "core:WaterDetectionState";
    public static final String CLOUD_DEVICE_STATUS_STATE = "core:CloudDeviceStatusState";
    public static final String BATTERY_LEVEL_STATE = "core:BatteryLevelState";
    public static final String SIREN_STATUS_STATE = "internal:SirenStatusState";
    public static final String TARGET_TEMPERATURE_STATE = "core:TargetTemperatureState";
    public static final String TARGET_ROOM_TEMPERATURE_STATE = "core:TargetRoomTemperatureState";
    public static final String SMOKE_STATE = "core:SmokeState";
    public static final String SENSOR_DEFECT_STATE = "core:SensorDefectState";
    public static final String RADIO_PART_BATTERY_STATE = "io:MaintenanceRadioPartBatteryState";
    public static final String SENSOR_PART_BATTERY_STATE = "io:MaintenanceSensorPartBatteryState";
    public static final String ZWAVE_SET_POINT_TYPE_STATE = "zwave:SetPointTypeState";

    // supported uiClasses
    public static final String CLASS_ROLLER_SHUTTER = "RollerShutter";
    public static final String CLASS_SCREEN = "Screen";
    public static final String CLASS_VENETIAN_BLIND = "VenetianBlind";
    public static final String CLASS_EXTERIOR_SCREEN = "ExteriorScreen";
    public static final String CLASS_EXTERIOR_VENETIAN_BLIND = "ExteriorVenetianBlind";
    public static final String CLASS_GARAGE_DOOR = "GarageDoor";
    public static final String CLASS_AWNING = "Awning";
    public static final String CLASS_ON_OFF = "OnOff";
    public static final String CLASS_LIGHT = "Light";
    public static final String CLASS_LIGHT_SENSOR = "LightSensor";
    public static final String CLASS_SMOKE_SENSOR = "SmokeSensor";
    public static final String CLASS_CONTACT_SENSOR = "ContactSensor";
    public static final String CLASS_OCCUPANCY_SENSOR = "OccupancySensor";
    public static final String CLASS_HUMIDITY_SENSOR = "HumiditySensor";
    public static final String CLASS_WINDOW = "Window";
    public static final String CLASS_ALARM = "Alarm";
    public static final String CLASS_POD = "Pod";
    public static final String CLASS_HEATING_SYSTEM = "HeatingSystem";
    public static final String CLASS_EXTERIOR_HEATING_SYSTEM = "ExteriorHeatingSystem";
    public static final String CLASS_DOOR_LOCK = "DoorLock";
    public static final String CLASS_PERGOLA = "Pergola";
    public static final String CLASS_WINDOW_HANDLE = "WindowHandle";
    public static final String CLASS_TEMPERATURE_SENSOR = "TemperatureSensor";
    public static final String CLASS_GATE = "Gate";
    public static final String CLASS_CURTAIN = "Curtain";
    public static final String CLASS_ELECTRICITY_SENSOR = "ElectricitySensor";
    public static final String CLASS_DOCK = "Dock";
    public static final String CLASS_SIREN = "Siren";
    public static final String CLASS_ADJUSTABLE_SLATS_ROLLER_SHUTTER = "AdjustableSlatsRollerShutter";
    public static final String CLASS_CAMERA = "Camera";

    // unsupported uiClasses
    public static final String THING_PROTOCOL_GATEWAY = "ProtocolGateway";
    public static final String THING_REMOTE_CONTROLLER = "RemoteController";
    public static final String THING_NETWORK_COMPONENT = "NetworkComponent";

    // Event states
    public static final String FAILED_EVENT = "FAILED";
    public static final String COMPLETED_EVENT = "COMPLETED";

    // supported thing types for discovery
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY,
            THING_TYPE_ROLLERSHUTTER, THING_TYPE_ROLLERSHUTTER_SILENT, THING_TYPE_SCREEN, THING_TYPE_VENETIANBLIND,
            THING_TYPE_EXTERIORSCREEN, THING_TYPE_EXTERIORVENETIANBLIND, THING_TYPE_GARAGEDOOR, THING_TYPE_AWNING,
            THING_TYPE_ACTIONGROUP, THING_TYPE_ONOFF, THING_TYPE_LIGHT, THING_TYPE_LIGHTSENSOR, THING_TYPE_SMOKESENSOR,
            THING_TYPE_CONTACTSENSOR, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_WINDOW, THING_TYPE_INTERNAL_ALARM,
            THING_TYPE_EXTERNAL_ALARM, THING_TYPE_POD, THING_TYPE_ZWAVE_HEATING_SYSTEM, THING_TYPE_ONOFF_HEATING_SYSTEM,
            THING_TYPE_DOOR_LOCK, THING_TYPE_PERGOLA, THING_TYPE_WINDOW_HANDLE, THING_TYPE_TEMPERATURESENSOR,
            THING_TYPE_GATE, THING_TYPE_CURTAIN, THING_TYPE_ELECTRICITYSENSOR, THING_TYPE_DOCK, THING_TYPE_SIREN,
            THING_TYPE_ADJUSTABLE_SLATS_ROLLERSHUTTER, THING_TYPE_MYFOX_CAMERA, THING_TYPE_ROLLERSHUTTER_UNO,
            THING_TYPE_WATERSENSOR, THING_TYPE_HUMIDITYSENSOR, THING_TYPE_MYFOX_ALARM, THING_TYPE_THERMOSTAT,
            THING_TYPE_DIMMER_LIGHT, THING_TYPE_EXTERIOR_HEATING_SYSTEM, THING_TYPE_VALVE_HEATING_SYSTEM,
            THING_TYPE_BIOCLIMATIC_PERGOLA));

    // somfy gateways
    public static Map<Integer, String> gatewayTypes = new HashMap<Integer, String>() {
        {
            put(0, "VIRTUAL_KIZBOX");
            put(2, "KIZBOX_V1");
            put(15, "TAHOMA");
            put(20, "VERISURE_ALARM_SYSTEM");
            put(21, "KIZBOX_MINI");
            put(24, "KIZBOX_V2");
            put(25, "MYFOX_ALARM_SYSTEM");
            put(27, "KIZBOX_MINI_VMBUS");
            put(28, "KIZBOX_MINI_IO");
            put(29, "TAHOMA_V2");
            put(30, "KIZBOX_V2_3H");
            put(31, "KIZBOX_V2_2H");
            put(32, "COZYTOUCH");
            put(34, "CONNEXOON");
            put(35, "JSW_CAMERA");
            put(37, "KIZBOX_MINI_DAUGHTERBOARD");
            put(38, "KIZBOX_MINI_DAUGHTERBOARD_ZWAVE");
            put(39, "KIZBOX_MINI_DAUGHTERBOARD_ENOCEAN");
            put(40, "KIZBOX_MINI_RAILDIN");
            put(41, "TAHOMA_V2_RTS");
            put(42, "KIZBOX_MINI_MODBUS");
            put(43, "KIZBOX_MINI_OVP");
            put(53, "CONNEXOON_RTS");
            put(54, "OPENDOORS_LOCK_SYSTEM");
            put(56, "CONNEXOON_RTS_JAPAN");
            put(58, "HOME_PROTECT_SYSTEM");
            put(62, "CONNEXOON_RTS_AUSTRALIA");
            put(63, "THERMOSTAT_SOMFY_SYSTEM");
            put(64, "BOX_ULTRA_LOW_COST_RTS");
            put(65, "SMARTLY_MINI_DAUGHTERBOARD_ZWAVE");
            put(66, "SMARTLY_MINIBOX_RAILDIN");
            put(67, "TAHOMA_BEE");
            put(72, "TAHOMA_RAIL_DIN");
            put(77, "ELIOT");
            put(88, "WISER");
        }
    };
}
