# Shelly Binding

This Binding integrates [Shelly devices](https://shelly.cloud) devloped by Allterco.

![](https://shop.shelly.cloud/image/cache/catalog/shelly_1/s1_x1-80x80.jpg)  ![](https://shop.shelly.cloud/image/cache/catalog/shelly_dimmer2/shelly_dimmer2_x1-80x80.jpg)  ![](https://shop.shelly.cloud/image/cache/catalog/shelly_vintage/shelly_vintage_A60-80x80.jpg)   ![](https://shop.shelly.cloud/image/cache/catalog/shelly_plug_s/s_plug_s_x1-80x80.jpg)   ![](https://shop.shelly.cloud/image/cache/catalog/shelly_button1/shelly_button1_x1-80x80.jpg)   ![](https://shop.shelly.cloud/image/cache/catalog/shelly_gas/shelly_gas_eu-80x80.jpg)   ![](https://shop.shelly.cloud/image/cache/catalog/shelly_ht/s_ht_x1-80x80.jpg)

Allterco provides a rich set of smart home devices. All of them are WiFi enabled (2,4GHz, IPv4 only) and provide a documented API. 
The binding is officially acknowledged by Allterco and openHAB is listed as a reference and directly supports the openHAB community.

The binding controls the devices independently from the Allterco Shelly Cloud (in fact it can be disabled).
The binding co-exists with Shelly App for Smartphones, Shelly Device Web UI, Shelly Cloud, MQTT and other 3rd party Apps.

The binding focuses on reporting the device status and device control.
Initial setup and device configuration has to be performed using the Shelly Apps (Web UI or Smartphone App).
The binding gets in sync with the next status refresh.

Refer to [Advanced Users](doc/AdvancedUsers.md) for more information on openHAB Shelly integration, e.g. firmware update, network communication or log filtering.

Also check out the [Shelly Manager](doc/ShellyManager.md), which

- provides detailed information on your Shellys
- helps to diagnose WiFi issues or device instabilities
- includes some common actions and 
- simplifies firmware updates.

[Shelly Manager](doc/ShellyManager.md) could also act as a firmware upgrade proxy - the device doesn't need to connect directly to the Internet, instead openHAB services as a download proxy, which improves device security.

## Supported Devices

| thing-type         | Model                                                  | Vendor ID |
|--------------------|--------------------------------------------------------|-----------|
| shelly1            | Shelly 1 Single Relay Switch                           | SHSW-1    |
| shelly1l           | Shelly 1L Single Relay Switch                          | SHSW-L    |
| shelly1pm          | Shelly Single Relay Switch with integrated Power Meter | SHSW-PM   |
| shelly2-relay      | Shelly Double Relay Switch in relay mode               | SHSW-21   |
| shelly2-roller     | Shelly2 in Roller Mode                                 | SHSW-21   |
| shelly25-relay     | Shelly 2.5 in Relay Switch                             | SHSW-25   |
| shelly25-roller    | Shelly 2.5 in Roller Mode                              | SHSW-25   |
| shelly4pro         | Shelly 4x Relay Switch                                 | SHSW-44   |
| shellydimmer       | Shelly Dimmer                                          | SHDM-1    |
| shellydimmer2      | Shelly Dimmer2                                         | SHDM-2    |
| shellyix3          | Shelly ix3                                             | SHIX3-1   |
| shellyuni          | Shelly UNI                                             | SHUNI-1   |
| shellyplug         | Shelly Plug                                            | SHPLG2-1  |
| shellyplugs        | Shelly Plug-S                                          | SHPLG-S   |
| shellyem           | Shelly EM with integrated Power Meters                 | SHEM      |
| shellyem3          | Shelly 3EM with 3 integrated Power Meter               | SHEM-3    |
| shellyrgbw2        | Shelly RGB Controller                                  | SHRGBW2   |
| shellybulb         | Shelly Bulb in Color or White Mode                     | SHBLB-1   |
| shellybulbduo      | Shelly Duo White                                       | SHBDUO-1  |
| shellybulbduo      | Shelly Duo White G10                                   | SHBDUO-1  |
| shellycolorbulb    | Shelly Duo Color G10                                   | SHCB-1    |
| shellyvintage      | Shelly Vintage (White Mode)                            | SHVIN-1   |
| shellyht           | Shelly Sensor (temp+humidity)                          | SHHT-1    |
| shellyflood        | Shelly Flood Sensor                                    | SHWT-1    |
| shellysmoke        | Shelly Smoke Sensor                                    | SHSM-1    |
| shellymotion       | Shelly Motion Sensor                                   | SHMOS-01  |
| shellygas          | Shelly Gas Sensor                                      | SHGS-1    |
| shellydw           | Shelly Door/Window                                     | SHDW-1    |
| shellydw2          | Shelly Door/Window 2                                   | SHDW-2    |
| shellybutton1      | Shelly Button 1                                        | SHBTN-1   |
| shellysense        | Shelly Motion and IR Controller                        | SHSEN-1   |
| shellydevice       | A password protected Shelly device or an unknown type  |           |

## Binding Configuration

The binding has the following configuration options:

| Parameter      |Description                                                       |Mandatory|Default                                         |
|----------------|------------------------------------------------------------------|---------|------------------------------------------------|
| defaultUserId  |Default user id for HTTP authentication when not set in the Thing |    no   |admin                                           |
| defaultPassword|Default password for HTTP authentication when not set in the Thing|    no   |admin                                           |
| autoCoIoT      |Auto-enable CoIoT events when firmware 1.6+ is enabled.           |    no   |true                                            |

The binding defaults to CoIoT events when firmware 1.6 or newer is detected. CoIoT provides near-realtime updates on device status changes.
This mode also overrules event settings in the Thing configuration. 

Disabling this feature allows granular control, which event types will be used. This is also required when the Shelly devices are not located on the same IP subnet (e.g. using a VPN).
In this case autoCoIoT should be disabled, CoIoT events will not work, because the underlying CoAP protocol is based on Multicast IP, which usually doesn't passes a VPN or routed network.

## Firmware

The binding requires firmware version 1.7.0 or newer to enable all features, version 1.9.2 is recommended.
Some of the features are enabled dynamically or are not available depending on device type and firmware release.
The Web UI of the Shelly device displays the current firmware version under Settings:Firmware and shows an update option when a newer version is available.

The current firmware version is reported in the Thing Properties.
A dedicated channel (device#updateAvailable) indicates the availability of a newer firmware.
Use the device's Web UI or the Shelly App to perform the update.

Check [Advanced Users](doc/AdvancedUsers.md) for information how to update your device.

Once you have updated the device **you should delete and re-discover** the openHAB Thing.
This makes sure that the Thing is correctly initialized and all supported channels are created.
openHAB will restore the channel/item linkage from the previous configuration, there is no need to re-create those manually.

## Discovery

In general devices need to be active to be discovered by the binding.
Battery powered devices need to wake up by pressing the button, they will stay active for a minute or so, which gives you enough time to start the discovery.

The binding uses mDNS to discover the Shelly devices. 
They periodically announce their presence, which is used by the binding to find them on the local network.

Sometimes you need to run the manual discovery multiple times until you see all your devices.

### Password Protected Devices

The Shelly devices can be configured to require authorization through a user id and password.
In this case you need to set these values in the Thing configuration after approving the Inbox entry.

If you have multiple devices protected by the same credentials it's recommended to set the default user id and password in the binding configuration BEFORE running the discovery.
In this case the binding could directly access the device to retrieve the required information using those credentials.
Otherwise a Thing of type shellyprotected is created in the Inbox and you could set the credentials while adding the Thing.
In this case the credentials are persisted as part of the Thing configuration. 

### Dynamic creation of channels

The Shelly series of devices has many combinations of relays, meters (different versions), sensors etc. 
For this the binding creates various channels dynamically based on the status information provided by the device at initialization time. 
If a channel is missing make sure the Thing was discovered correctly and is ONLINE.
If a channel is missing delete the Thing and re-discover it.
Creation of those channels takes about 5-10sec, maybe you need to reload the page to update the browser status.

### Important for battery powered devices

Make sure to wake up battery powered devices, so that they show up on the network.
The Shelly Button 1 needs to be connected to USB, other devices like Flood have a push button inside.
For those open the case, press that button and the LED starts flashing.
Wait a moment and then start the discovery. The device should show up in the Inbox and can be added.
Sometimes you need to run the discovery multiple times.

### Roller Favorites

Firmware 1.9.2 for Shelly 2.5 in roller mode supports so called favorites for positions.
You could use the Shelly App to setup 4 different positions (percentage) and assign id 1-4.
The channel `roller#rollerFav` allows to select those from openHAB and the roller moves to the desired position.
In the Thing configuration you could also configure an id when the `roller#control` channel receives UP or DOWN.
Values 1-4 are selecting the corresponding favorite id in the Shelly App, 0 means no favorite.

### Thing Status

The binding sets the following Thing status depending on the device status:

| Status       |Description                                                       |
|--------------|------------------------------------------------------------------|
| INITIALIZING | This is the default status while initializing the Thing. Once the initialization is triggered the Thing switches to Status UNKNOWN. |
| UNKNOWN      | Indicates that the status is currently unknown, which must not show a problem. Usually the Thing stays in this status when the device is in sleep mode. Once the device is reachable and was initialized the Thing switches to status ONLINE.|
| ONLINE       | ONLINE indicates that the device can be accessed and is responding properly. Battery powered devices also stay ONLINE when in sleep mode. The binding has an integrated watchdog timer supervising the device, see below. The Thing switches to status OFFLINE when some type of communication error occurs. | 
| OFFLINE      | Communication with the device failed. Check the Thing status in the UI and openHAB's log for an indication of the error. Try restarting OH or deleting and re-discovering the Thing. You could also post to the community thread if the problem persists. |

`Battery powered devices:` 
If the device is in sleep mode and can't be reached by the binding, the Thing will change into UNKNOWN state.
Once the device wakes up, the Thing will perform initialization and the state will change to ONLINE.

The first time a device is discovered and initialized successfully, the binding will be able to perform auto-initialization when OH is restarted.  Waking up the device triggers the event URL and/or CoIoT packet, which is processed by the binding and triggers initialization. Once a device is initialized, it is no longer necessary to manually wake it up after an openHAB restart unless you change the battery. In this case press the button and run the discovery again.

### Device Watchdog

The binding supervises availability of the device once it becomes ONLINE by sending periodic status requests to the device. The watchdog is restarted when the device is responding properly.

Communication errors are handled depending on the device type:

- regular power devices change to OFFLINE, because this status indicates an error
- battery powered devices stay ONLINE, because usually the device is in sleep mode

The binding also monitors that the device is responding at least once within a given time period.
The period is computed depending on the device type and configuration:

- battery  powered devices: &lt;sleepPeriod from device config&gt; + 10min, usually 12h+10min=730min
- else, if CoIoT is enabled: 3*&lt;update Period from device settings&gt;+10sec, usually3*15+10=45sec
- else 2*60+10sec = 130sec

Once the timer expires the device switches to OFFFLINE and the bindings starts to re-initialize the device periodically. 

You could also create a rule to catch those status changes or device alarms (see rule examples).

## Thing Configuration

|Parameter         |Description                                                   |Mandatory|Default                                           |
|------------------|--------------------------------------------------------------|---------|--------------------------------------------------|
|deviceIp          |IP address of the Shelly device                               |    yes  |none                                              |
|userId            |The user id used for HTTP authentication                      |    no   |none                                              |
|password          |Password for HTTP authentication                              |    no   |none                                              |
|brightnessAutoOn  |true: Output will be activated when brightness > 0 is set     |    no   |true                                              |
|lowBattery        |Threshold for battery level. Set alert when level is below.   |    no   |20 (=20%), only for battery powered devices       |
|updateInterval    |Interval for the background status check in seconds.          |    no   |1h for battery powered devices, 60s for all others|
|eventsButton      |true: register event "trigger when a button is pushed"        |    no   |false                                             |
|eventsPush        |true: register event "trigger on short and long push"         |    no   |false                                             |
|eventsSwitch      |true: register event "trigger of switching the relay output"  |    no   |true                                              |
|eventsSensorReport|true: register event "posted updated sensor data"             |    no   |true for sensor devices                           |
|eventsCoIoT       |true: Listen for CoIoT/COAP events                            |    no   |true for battery devices, false for others        |
|eventsRoller      |true: register event "trigger" when the roller updates status |    no   |true for roller devices                           |
|favoriteUP        |0-4: Favorite id for UP (see Roller Favorites)                |    no   |0 = no favorite id                                |
|favoriteDOWN      |0-4: Favorite id for DOWN (see Roller Favorites)              |    no   |0 = no favorite id                                |


### General Notes

Every device has a channel group `device` with the following channels:

|Group     |Channel            |Type    |read-only|Description                                                                      |
|----------|-------------------|--------|---------|---------------------------------------------------------------------------------|
|device    |deviceName         |String  |yes      |Device name as configured in the Shelly App                                      |
|          |uptime             |Number  |yes      |Number of seconds since the device was powered up                                |
|          |wifiSignal         |Number  |yes      |WiFi signal strength (4=excellent, 3=good, 2=not string, 1=unreliable, 0=none)   |
|          |internalTemp       |Number  |yes      |Internal device temperature (when provided by the device)                        |
|          |selfTest           |String  |yes      |Result from device self-test (pending/not_completed/running/completed/unknown)   |
|          |alarm              |Trigger |yes      |Self-Test result not_completed/completed/running/pending                         |
|          |supplyVoltage      |Number  |yes      |Shelly 1PM, 1L, 2.5: Supply voltage (fixed or measured depending on device)      |
|          |accumulatedWatts   |Number  |yes      |Accumulated power in W of the device (including all meters)                      |
|          |accumulatedTotal   |Number  |yes      |Accumulated total power in kwh of the device (including all meters)              |
|          |accumulatedReturned|Number  |yes      |Accumulated returned power in kwh of the device (including all meters)           |
|          |heartBeat          |DateTime|yes      |Timestamp of the last successful device communication                            |
|          |updateAvailable    |Switch  |yes      |ON: A firmware update is available                                               |
|          |statusLed          |Switch  |r/w      |ON: Status LED is disabled, OFF: LED enabled                                     |
|          |powerLed           |Switch  |r/w      |ON: Power LED is disabled, OFF: LED enabled                                      |
|          |charger            |Switch  |yes      |ON: USB charging cable is connected external power supply activated.             |

Availability of channels is depending on the device type.
The binding detects many of those channels on-the-fly (when Thing changes to ONLINE state) and adjusts the Thing's channel structure.
The device must be discovered and ONLINE to successfully complete this process.
The accumulated channels are only available for devices with more than 1 meter. accumulatedReturned only for the EM and 3EM.
The LED channels are available for the Plug-S with firmware 1.6x and for various other devices with firmware 1.8 or newer. The binding detects them automatically.

## Events

### Action URLs vs. CoIoT

Depending on the firmware release the Shelly devices supports 2 different mechanims to report sensor updates or events.

1. Action URLs

Usually the binding polls the device to update the status and maps the returned values to the various channels.
In addition the binding can register so-called Action URLs. Those events are triggered by the device to report special events.
You need to disable autoCoIoT in the binding configuration to make specific selections for the Action events.

The following event types could be registered when enabled in the Thing configuration:

|Event Type        |Description                                                                                                    |
|------------------|---------------------------------------------------------------------------------------------------------------|
|eventsButton      |This event is triggered when the device is in button mode. The device reports the ON/OFF status of the button. |
|eventsSwitch      |This event reports the status of the relay output. This could change by the button or API calls.               |
|eventsPush        |The device reports the short/longpush events when in  button mode momentary, momentary_on_release, one_button or two_button |
|eventsSensorReport|Sensor devices (like H&T) provide sensor updates when this action URL is enabled.                              |

Important: The binding defaults to CoIoT when firmware 1.6 or newer is detected.
This has significant experience improvements and also prevents interfering with other applications, because the device only supports one set of Action URLs.

2. CoIoT / CoAP

Starting with version 1.6 the devices reports most status values via the CoIoT protocol.
CoIoT provides near-realtime updates and better event support.
Firmware 1.7 adds additional status values, also supported by the binding.
Version 1.8 introduces CoIoT version 2, which fixes various issues with version 1 and provides almost all relevant status updates.

If there is no specific reason you should enable CoIoT. Check section Network Settings [here](doc/AdvancedUsers.md) for more information.

Enable the autoCoIoT option in the binding configuration or eventsCoIoT in the Thing configuration to activate CoIoT.

### Button events

Various devices signal an event when the physical button is pressed.
This could be a switch connected to the SW input of the relay or the Button 1.

The following trigger types are sent:

|Event Type         |Description                                                                                                    |
|-------------------|---------------------------------------------------------------------------------------------------------------|
|SHORT_PRESSED      |The button was pressed once for a short time (lastEvent=S)                                                     |
|DOUBLE_PRESSED     |The button was pressed twice with short delay (lastEvent=SS)                                                   |
|TRIPLE_PRESSED     |The button was pressed three times with short delay (lastEvent=SSS)                                            |
|LONG_PRESSED       |The button was pressed for a longer time (lastEvent=L)                                                         |
|SHORT_LONG_PRESSED |A short followed by a long button push (lastEvent=SL)                                                          |
|LONG_SHORT_PRESSED |A long followed by a short button push (lastEvent=LS)                                                          |
 
Check the channel definitions for the various devices to see if the device supports those events.
You could use the Shelly App to set the timing for those events. 

If you want to use those events triggering a rule:

- If a physical switch is connected to the Shelly use the input channel(`input` or `input1`/`input2`) to trigger a rule
- For a momentary button use the `button` trigger channel as trigger, channels `lastEvent` and `eventCount` will provide details on the event 

### Alarms

The binding provides health monitoring functions for the device.
When an alarm condition is detected the channel alarm gets triggered and provides one of the following alarm types:

A new alarm will be triggered on a new condition or every 5 minutes if the condition persists.

### Non-battery powered devices

|Event Type|Description|
|------------|-----------------------------------------------------------------------------------------------------------------|
|RESTARTED   |The device has been restarted. This could be an indicator for a firmware problem.                                |
|WEAK_SIGNAL |An alarm is triggered when RSSI is < -80, which indicates an unstable connection.                                |
|OVER_TEMP   |The device is overheating, check installation and housing.                                                       |
|OVER_LOAD   |An over load condition has been detected, e.g. from the roller motor.                                            |
|OVER_POWER  |Maximum allowed power was exceeded. The relay was turned off.                                                    |
|LOAD_ERROR  |Device reported a load problem, so far Dimmer only.                                                              |

### Sensors

|Event Type|Description|
|------------|-----------------------------------------------------------------------------------------------------------------|
|POWERON     |Device was powered on.                                                                                           |
|PERIODIC    |Periodic wakeup.                                                                                                 |
|BUTTON      |Button was pressed, e.g. to wake up the device.                                                                  |
|SENSOR      |Wake-up due to updated sensor data.                                                                              |
|ALARM       |Alarm condition was detected, check status, could be OPENED for the DW, flood alarm, smoke alarm                 |
|BATTERY     |Device reported an update to the battery status.                                                                 |
|TEMP_UNDER  |Below "temperature under" threshold                                                                              |
|TEMP_OVER   |Above "temperature over" threshold                                                                               |
|VIBRATION   |A vibration/tamper was detected (DW2 only)                                                                       |

Refer to section [Full Example:shelly.rules](#shelly-rules) for examples how to catch alarm triggers in openHAB rules

## Channels

Depending on the device type and firmware release channels might be not available or stay with value NaN.  

### Shelly 1(thing-type: shelly1)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see general notes on channels                       |
|          |button       |Trigger  |yes      |Event trigger with payload, see SHORT_PRESSED or LONG_PRESSED                    |
|          |lastEvent    |String   |yes      |Last event type (S/SS/SSS/L)                                                     |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|sensors   |temperature1 |Number   |yes      |Temperature value of external sensor #1 (if connected to temp/hum addon)         |
|          |temperature2 |Number   |yes      |Temperature value of external sensor #2 (if connected to temp/hum addon)         |
|          |temperature3 |Number   |yes      |Temperature value of external sensor #3 (if connected to temp/hum addon)         |
|          |humidity     |Number   |yes      |Humidity in percent (if connected to temp/hum addon)                             |

### Shelly 1L (thing-type: shelly1l)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input1       |Switch   |yes      |ON: Input/Button for input 1 is powered, see general notes on channels           |
|          |button1      |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent1   |String   |yes      |Last event type (S/SS/SSS/L) for input 1                                         |
|          |eventCount1  |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |input2       |Switch   |yes      |ON: Input/Button for channel 2 is powered, see general notes on channels         |
|          |button2      |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent2   |String   |yes      |Last event type (S/SS/SSS/L) for input 2                                         |
|          |eventCount2  |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |
|sensors   |temperature1 |Number   |yes      |Temperature value of external sensor #1 (if connected to temp/hum addon)         |
|          |temperature2 |Number   |yes      |Temperature value of external sensor #2 (if connected to temp/hum addon)         |
|          |temperature3 |Number   |yes      |Temperature value of external sensor #3 (if connected to temp/hum addon)         |
|          |humidity     |Number   |yes      |Humidity in percent (if connected to temp/hum addon)                             |

Note: The `meter`for the Shelly 1L is kind of fake.
It doesn't have a real power meter, but you could setup an estimated consumption in the Shelly App, e.g. 60W if you have attached a good old light bulb to the output channel.
In this case the is no real measurement based on power consumption, but the Shelly reports the configured value when the relay is ON.

### Shelly 1PM (thing-type: shelly1pm)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption for a round minute, 1 minute  ago                   |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |
|sensors   |temperature1 |Number   |yes      |Temperature value of external sensor #1 (if connected to temp/hum addon)         |
|          |temperature2 |Number   |yes      |Temperature value of external sensor #2 (if connected to temp/hum addon)         |
|          |temperature3 |Number   |yes      |Temperature value of external sensor #3 (if connected to temp/hum addon)         |
|          |humidity     |Number   |yes      |Humidity in percent (if connected to temp/hum addon)                             |

### Shelly EM (thing-type: shellyem)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see general notes on channels                       |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent    |String   |yes      |Last event type (S/SS/SSS/L)                                                     |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|meter1    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |returnedKWH  |Number   |yes      |Total returned energy, kwh                                                       |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |powerFactor  |Number   |yes      |Power Factor in percent                                                          |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |
|meter2    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |returnedKWH  |Number   |yes      |Total returned energy, kwh                                                       |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |powerFactor  |Number   |yes      |Power Factor in percent                                                          |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |

### Shelly 3EM (thing-type: shellyem3)

Please note: The product is called Shelly 3EM whereas the device propagates the service under shellyem3. 
The Thing id is derived from the service name, so that's the reason why the Thing is named shelly**em3** and not shelly3em.

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see general notes on channels                       |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent    |String   |yes      |Last event type (S/SS/SSS/L)                                                     |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|meter1    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |returnedKWH  |Number   |yes      |Total returned energy, kwh                                                       |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |current      |Number   |yes      |Current in A                                                                     |
|          |powerFactor  |Number   |yes      |Power Factor in percent                                                          |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |
|meter2    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |returnedKWH  |Number   |yes      |Total returned energy, kwh                                                       |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |current      |Number   |yes      |Current in A                                                                     |
|          |powerFactor  |Number   |yes      |Power Factor in percent                                                          |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |
|meter3    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |returnedKWH  |Number   |yes      |Total returned energy, kwh                                                       |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |current      |Number   |yes      |Current in A                                                                     |
|          |powerFactor  |Number   |yes      |Power Factor in percent                                                          |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |


### Shelly 2 - relay mode thing-type: shelly2-relay)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |output       |Switch   |r/w      |Relay #1: Controls the relay's output channel (on/off)                           |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|relay2    |output       |Switch   |r/w      |Relay #2: Controls the relay's output channel (on/off)                           |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |autoOn       |Number   |r/w      |Relay #2: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #2: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #2: ON: An auto-on/off timer is active                                     |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption for a round minute, 1 minute  ago                             |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |

### Shelly 2 - roller mode thing-type: shelly2-roller)

|Group     |Channel      |Type     |read-only|Description                                                                           |
|----------|-------------|---------|---------|--------------------------------------------------------------------------------------|
|roller    |control      |Rollershutter|r/w  |can be open (0%), stop, or close (100%); could also handle ON (open) and OFF (close)  |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                            |
|          |event        |Trigger  |yes      |Roller event/trigger with payload ROLLER_OPEN / ROLLER_CLOSE / ROLLER_STOP            |
|          |rollerpos    |Number   |r/w      |Roller position: 100%=open...0%=closed; gets updated when the roller stops, see Notes |
|          |rollerFav    |Number   |r/w      |Select roller position favorite (1-4, 0=no), see Notes                                |
|          |state        |String   |yes      |Roller state: open/close/stop                                                         |
|          |stopReason   |String   |yes      |Last stop reasons: normal, safety_switch or obstacle                                  |
|          |safety       |Switch   |yes      |Indicates status of the Safety Switch, ON=problem detected, powered off               |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                                    |
|          |lastPower1   |Number   |yes      |Accumulated energy consumption in Watts for the full last minute                      |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart)      |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                     |

The roller positioning calibration has to be performed using the Shelly App before the position can be set in percent.

Refer to [Smartify Roller Shutters with openHAB and Shelly](doc/UseCaseSmartRoller.md) for more information on roller integration. 

### Shelly 2.5 - relay mode (thing-type:shelly25-relay) 

The Shelly 2.5 includes 2 meters, one for each channel.
Firmware 1.9.2 or newer is required to use the roller position favorites, which are defined in the Shelly App.


|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay2    |             |         |         |See group relay1 for Shelly 2                                                    |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly 2.5 - roller mode (thing-type: shelly25-roller)

The Shelly 2.5 includes 2 meters, one for each channel.
However, it doesn't make sense to differ power consumption for the roller moving up vs. moving down.
For this the binding aggregates the power consumption of both relays and includes the values in "meter1".

|Group     |Channel      |Type     |read-only|Description                                                                          |
|----------|-------------|---------|---------|-------------------------------------------------------------------------------------|
|roller    |control      |Rollershutter |r/w |can be open (0%), stop, or close (100%); could also handle ON (open) and OFF (close) |
|          |rollerpos    |Dimmer   |r/w      |Roller position: 100%=open...0%=closed; gets updated when the roller stopped         |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                           |
|          |state        |String   |yes      |Roller state: open/close/stop                                                        |
|          |stopReason   |String   |yes      |Last stop reasons: normal, safety_switch or obstacle                                 |
|          |safety       |Switch   |yes      |Indicates status of the Safety Switch, ON=problem detected, powered off              |
|          |event        |Trigger  |yes      |Roller event/trigger with payload ROLLER_OPEN / ROLLER_CLOSE / ROLLER_STOP           |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                        |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                        |

The roller positioning calibration has to be performed using the Shelly App before the position can be set in percent. 

Refer to [Smartify Roller Shutters with openHAB and Shelly](doc/UseCaseSmartRoller.md) for more information on roller integration. 

### Shelly4 Pro (thing-type: shelly4pro)

The Shelly 4Pro provides 4 relays and 4 power meters.
 
|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay2    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay3    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay4    |             |         |         |See group relay1 for Shelly 2                                                    |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter3    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter4    |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly Plug-S (thing-type: shellyplugs)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |             |         |         |See group relay1 for Shelly 2                                                    |
|meter     |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly Dimmer 1 + 2 (thing-type: shellydimmer, shellydimmer2)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |brightness   |Dimmer   |r/w      |Currently selected brightness.                                                   |
|          |outputName   |String   |yes      |Logical name of this relay output as configured in the Shelly App                |
|          |input1       |Switch   |yes      |ON: Input/Button for input 1 is powered, see general notes on channels           |
|          |button1      |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent1   |String   |yes      |Last event type (S/SS/SSS/L) for input 1                                         |
|          |eventCount1  |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |input2       |Switch   |yes      |ON: Input/Button for channel 2 is powered, see general notes on channels         |
|          |button2      |Trigger  |yes      |Event trigger, see section Button Events                                         |
|          |lastEvent2   |String   |yes      |Last event type (S/SS/SSS/L) for input 2                                         |
|          |eventCount2  |Number   |yes      |Counter gets incremented every time the device issues a button event.            |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption for a round minute, 1 minute  ago                             |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                                |


`Note: The Dimmer should be calibrated using the Shelly App.`

Using the Thing configuration option `brightnessAutoOn` you could decide if the light is turned on when a brightness > 0 is set.
`true`:  Brightness will be set and device output is powered = light turns on with the new brightness
`false`: Brightness will be set, but output stays unchanged so light will not be switched on when it's currently off.

### Shelly ix3 (thing-type: shellyix3)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|status    |input1       |Switch   |yes      |State of Input 1                                                       |
|          |input2       |Switch   |yes      |State of Input 2                                                       |
|          |input3       |Switch   |yes      |State of Input 3                                                       |
|          |button       |Trigger  |yes      |Event trigger: Event trigger, see section Button Events                |
|          |lastEvent    |String   |yes      |S/SS/SSS for 1/2/3x Shortpush or L for Longpush                        |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.  |

### Shelly UNI - Low voltage sensor/actor: shellyuni)

|Group     |Channel      |Type     |read-only|Description                                                                 |
|----------|-------------|---------|---------|----------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2, no autoOn/autoOff/timerActive channels       |
|relay2    |             |         |         |See group relay1 for Shelly 2, no autoOn/autoOff/timerActive channels       |
|sensors   |temperature1 |Number   |yes      |Temperature value of external sensor #1 (if connected to temp/hum addon)    |
|          |temperature2 |Number   |yes      |Temperature value of external sensor #2 (if connected to temp/hum addon)    |
|          |temperature3 |Number   |yes      |Temperature value of external sensor #3 (if connected to temp/hum addon)    |
|          |humidity     |Number   |yes      |Humidity in percent (if connected to temp/hum addon)                        |
|          |voltage      |Number   |yes      |ADCS voltage                                                                |
|status    |input1       |Switch   |yes      |State of Input 1                                                            |
|          |input2       |Switch   |yes      |State of Input 2                                                            |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                                    |
|          |lastEvent    |String   |yes      |S/SS/SSS for 1/2/3x Shortpush or L for Longpush                             |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.       |

### Shelly Bulb (thing-type: shellybulb)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |mode         |Switch   |r/w      |Color mode: color or white                                             |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF; in sec            |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON: in sec            |
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|color     |             |         |         |Color settings: only valid in COLOR mode                               |
|          |hsb          |HSB      |r/w      |Represents the color picker (HSBType), control r/g/b, but not white    |
|          |full         |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           |
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |red          |Dimmer   |r/w      |Red brightness: 0..100% or 0..255 (control only the red channel)       |
|          |green        |Dimmer   |r/w      |Green brightness: 0..100% or 0..255 (control only the green channel)   |
|          |blue         |Dimmer   |r/w      |Blue brightness: 0..100% or 0..255 (control only the blue channel)     |
|          |white        |Dimmer   |r/w      |White brightness: 0..100% or 0..255 (control only the white channel)   |
|          |gain         |Dimmer   |r/w      |Gain setting: 0..100%     or 0..100                                    |
|          |effect       |Number   |r/w      |Puts the light into effect mode: 0..6)                                 |
|          |             |         |         |  0=No effect, 1=Meteor Shows, 2=Gradual Change, 3=Breath              |
|          |             |         |         |  4=Flash, 5=On/Off Gradual, 6=Red/Green Change                        |
|white     |             |         |         |Color settings: only valid in WHITE mode                               |
|          |temperature  |Number   |r/w      |color temperature (K): 0..100% or 3000..6500                           |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |

Note: The openHAB color picker has only values for red/green/blue (RGB), not for white as supported by the RGBW2.
Beside channel `hsb` the binding also offers the `white` channel (hsb as only RGB values).
Or control each color separately with channels `red`, `blue`, `green` (those are advanced channels).


#### Shelly Duo (thing-type: shellybulbduo)

This information applies to the Shelly Duo-1 as well as the Duo White for the G10 socket.

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF; in sec            |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON: in sec            |
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|white     |             |         |         |Color settings: only valid in WHITE mode                               |
|          |temperature  |Number   |r/w      |color temperature (K): 0..100% or 2700..6500                           |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago          |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (resets on restart)|
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                      |
 
#### Shelly Vintage (thing-type: shellyvintage)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF; in sec            |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON: in sec            |
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|white     |             |         |         |Color settings: only valid in WHITE mode                               |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |
|          |lastPower1   |Number   |yes      |Energy consumption for a round minute, 1 minute  ago                   |
|          |totalKWH     |Number   |yes      |Total energy consumption in kWh since the device powered up (resets on restart)|
|          |lastUpdate   |DateTime |yes      |Timestamp of the last measurement                                      |
 
## Shelly Duo Color (thing-type: shellyduocolor-color)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|color     |             |         |         |Color settings: only valid in COLOR mode                               |
|          |hsb          |HSB      |r/w      |Represents the color picker (HSBType), control r/g/b, but not white    |
|          |full         |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           |
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |red          |Dimmer   |r/w      |Red brightness: 0..100% or 0..255 (control only the red channel)       |
|          |green        |Dimmer   |r/w      |Green brightness: 0..100% or 0..255 (control only the green channel)   |
|          |blue         |Dimmer   |r/w      |Blue brightness: 0..100% or 0..255 (control only the blue channel)     |
|          |white        |Dimmer   |r/w      |White brightness: 0..100% or 0..255 (control only the white channel)   |
|          |gain         |Dimmer   |r/w      |Gain setting: 0..100%     or 0..100                                    |
|          |effect       |Number   |r/w      |Puts the light into effect mode: 0=No effect, 1=Meteor Shower, 2=Gradual Change, 3=Flash |
|white     |             |         |         |Color settings: only valid in WHITE mode                               |
|          |temperature  |Number   |r/w      |color temperature (K): 0..100% or 3000..6500                           |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |

Using the Thing configuration option `brightnessAutoOn` you could decide if the light is turned on when a brightness > 0 is set.
`true`:  Brightness will be set and device output is powered = light turns on with the new brightness
`false`: Brightness will be set, but output stays unchanged so light will not be switched on when it's currently off.


## Shelly Duo RGBW Color Bulb (thing-type: shellycolorbulb)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|color     |             |         |         |Color settings: only valid in COLOR mode                               |
|          |hsb          |HSB      |r/w      |Represents the color picker (HSBType), control r/g/b, bight not white  |
|          |full         |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           |
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |red          |Dimmer   |r/w      |Red brightness: 0..100% or 0..255 (control only the red channel)       |
|          |green        |Dimmer   |r/w      |Green brightness: 0..100% or 0..255 (control only the green channel)   |
|          |blue         |Dimmer   |r/w      |Blue brightness: 0..100% or 0..255 (control only the blue channel)     |
|          |white        |Dimmer   |r/w      |White brightness: 0..100% or 0..255 (control only the white channel)   |
|          |gain         |Dimmer   |r/w      |Gain setting: 0..100%     or 0..100                                    |
|          |effect       |Number   |r/w      |Puts the light into effect mode: 0..3)                                 |
|          |             |         |         |0=No effect, 1=Meteor Shower, 2=Gradual Change, 3=Flash                |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |

Channels in group `color`or `white`apply depending on the selected mode - they are not active at the same time. 

Using the Thing configuration option `brightnessAutoOn` you could decide if the light is turned on when a brightness > 0 is set.
`true`:  Brightness will be set and device output is powered = light turns on with the new brightness
`false`: Brightness will be set, but output stays unchanged so light will not be switched on when it's currently off.

### Shelly RGBW2 in White Mode (thing-type: shellyrgbw2-white)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |input        |Switch   |yes      |State of Input                                                         |
|channel1  |brightness   |Dimmer   |r/w      |Channel 1: Brightness: 0..100, control power state with ON/OFF         |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|channel2  |brightness   |Dimmer   |r/w      |Channel 2: Brightness: 0..100, control power state with ON/OFF         |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|channel3  |brightness   |Dimmer   |r/w      |Channel 3: Brightness: 0..100, control power state with ON/OFF         |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|channel4  |brightness   |Dimmer   |r/w      |Channel 4: Brightness: 0..100, control power state with ON/OFF         |
|          |button       |Trigger  |yes      |Event trigger, see section Button Events                               |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts (all channels)                      |

Please note that the settings of channel group color are only valid in color mode and vice versa for white mode.
The current firmware doesn't support the timestamp report for the meters. 
The binding emulates this by using the system time on every update.

In white mode each RGBW2 channel is defined as DimmableLight.
This means that the brightness channel has 2 functions

- Sending ON/OFF (OnOffType) to power on/off the channel
- Sending a Number to set the brightness (percentage 0..100)

Sending brightness 0 will automatically turn off the channel if it's currently on.
Sending brightness > 0 will automatically turn on the channel if it's currently off. 
You can define 2 items (1 Switch, 1 Number) mapping to the same channel, see example rules.

### Shelly H&T (thing-type: shellyht)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

`Please Note:` If you have connected an USB cable to the H&T, but channel charger is off make sure that "Use external power supply" settings is activated in the Shelly App's device settings.

### Shelly Flood (thing-type: shellyflood)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |flood        |Switch   |yes      |ON: Flooding condition detected, OFF: no flooding                      |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

### Shelly Door/Window (thing-type: shellydw, shellydw2)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |state        |Contact  |yes      |OPEN: Contact is open, CLOSED: Contact is closed                       |
|          |lux          |Number   |yes      |Brightness in Lux                                                      |
|          |illumination |String   |yes      |Current illumination: dark/twilight/bright                             |
|          |tilt         |Number   |yes      |Tilt in ° (angle), -1 indicates that the sensor is not calibrated      |
|          |vibration    |Switch   |yes      |ON: Vibration detected                                                 |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |
|          |lastError    |String   |yes      |Last device error.                                                     |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |
|device    |alarm        |Trigger  |yes      |Will receive trigger VIBRATION if DW2 detects vibration                |

### Shelly Motion (thing-type: shellymotion)

Important: The Shelly Motion does only support CoIoT Unicast, which means you need to set the CoIoT peer address.

- Use device WebUI, open COIOT settings, make sure CoIoT is enabled and enter the openHAB IP address or
- Use [Shelly Manager](doc/ShellyManager.md, select Action 'Set CoIoT peer' and the Manager will sets the openHAB IP address as peer address

|Group     |Channel        |Type     |read-only|Description                                                          |
|----------|---------------|---------|---------|---------------------------------------------------------------------|
|sensors   |motion         |Switch   |yes      |ON: Motion was detected                                              |
|          |motionTimestamp|DateTime |yes      |Time when motion started/was detected                                |
|          |lux            |Number   |yes      |Brightness in Lux                                                    |
|          |illumination   |String   |yes      |Current illumination: dark/twilight/bright                           |
|          |vibration      |Switch   |yes      |ON: Vibration detected                                               |
|          |charger        |Switch   |yes      |ON: USB charging cable is connected external power supply activated. |
|          |motionActive   |Switch   |yes      |ON: Motion detection is currently active                             |
|          |sensorSleepTime|Number   |no       |Specifies the number of sec the sensor should not report events      ]
|          |lastUpdate     |DateTime |yes      |Timestamp of the last update (any sensor value changed)              |
|battery   |batteryLevel   |Number   |yes      |Battery Level in %                                                   |
|          |lowBattery     |Switch   |yes      |Low battery alert (< 20%)                                            |

Use case for the 'sensorSleepTime': 
You have a Motion controlling your light. 
You switch off the light and want to leave the room, but the motion sensor immediately switches light back on.
Using 'sensorSleepTime' you could suppress motion events while leaving the room, e.g. for 5sec and the light doesn's switch on. 

### Shelly Button 1 (thing-type: shellybutton1)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|status    |lastEvent    |String   |yes      |S/SS/SSS for 1/2/3x Shortpush or L for Longpush                        |
|          |eventCount   |Number   |yes      |Counter gets incremented every time the device issues a button event.  |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels             |
|          |button       |Trigger  |yes      |Event trigger with payload SHORT_PRESSED, DOUBLE_PRESSED...            |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any value changed)                       |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

You should calibrate the sensor using the Shelly App to get information on the tilt status.

### Shelly Smoke (thing-type: shellysmoke)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |smoke        |Number   |yes      |ON: Smoke detected                                                     |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |
|          |lastError    |String   |yes      |Last device error.                                                     |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

### Shelly Smoke(thing-type: shellysmoke)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |ppm          |Number   |yes      |Gas concentration (ppm)                                                |
|          |sensorState  |String   |yes      |Sensor state: unknown/warmup/normal/fault                              |
|          |alarmState   |String   |yes      |Alarm state: unknown/none/mild/heavy/test                              |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |

### Shelly Sense (thing-type: shellysense) 

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |key          |String   |r/w      |Send a IR key to the sense. There a 3 different types supported        |
|          |             |         |         |Stored key: send the key code defined by the App , e.g. 123_1_up       |
|          |             |         |         |Pronto hex: send a Pronto Code in hex format, e.g. 0000 006C 0022 ...  |
|          |             |         |         |Pronto base64: in base64 format, will be send 1:1 to the Sense         |
|          |motionTime   |Number   |r/w      |Define the number of seconds when the Sense should report motion       |
|          |motionLED    |Switch   |r/w      |Control the motion LED: ON when motion is detected or OFF              |
|          |charger      |Switch   |yes      |ON: charger connected, OFF: charger not connected.                     |
|sensors   |temperature  |Number   |yes      |Temperature in °C                                                      |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|          |lux          |Number   |yes      |Brightness in Lux                                                      |
|          |motion       |Switch   |yes      |ON: Motion detected, OFF: No motion (check also motionTime)            |
|          |lastUpdate   |DateTime |yes      |Timestamp of the last update (any sensor value changed)                |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |batteryAlert |Switch   |yes      |Low battery alert                                                      |

## Full Example

### shelly.things

```
/* Shelly 2.5 Roller */
Thing shelly:shelly25-roller:XXXXX1 "Shelly 25 Roller XXXXX1" @ "Home Theater" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-roller:XXXXX2 "Shelly 25 Roller XXXXX2" @ "Living Room"  [deviceIp="x.x.x.x", userId="admin", password="secret"]


/* Shelly 2.5 Relays */
Thing shelly:shelly25-relay:XXXXX3 "Shelly 25 Relay XXXXX3" @ "Hall Way" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-relay:XXXXX4 "Shelly 25 Relay XXXXX4" @ "Dining Room" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-relay:XXXXX5 "Shelly 25 Relay XXXXX5" @ "Bed Room" [deviceIp="x.x.x.x", userId="", password=""]

/* Other *
Thing shelly:shellyht:e01691 "ShellyChimenea" @ "lowerground" [ deviceIp="10.0.55.101", userId="", password="", lowBattery=15 , eventsCoIoT=true ]
Thing shelly:shellyht:e01681 "ShellyDormitorio" @ "upperground" [ deviceIp="10.0.55.102", userId="", password="", lowBattery=15 , eventsCoIoT=true ]
Thing shelly:shellyflood:XXXXXX "ShellyFlood" @ "cellar" [ deviceIp="10.0.0.103", userId="", password="", lowBattery=15, eventsSwitch=true, eventsButton=true, eventsCoIoT=true ]

```

### shelly.items

```
/* Relays */
Switch Shelly_XXXXX3_Relay        "Garage Light"                  {channel="shelly:shelly1:XXXXX3:relay#output"}
Number Shelly_XXXXX3_AutoOnTimer  "Garage Light Auto On Timer"    {channel="shelly:shelly1:XXXXX3:relay#autoOn"}
Number Shelly_XXXXX3_AutoOffTimer "Garage Light Auto Off Timer"   {channel="shelly:shelly1:BA2F18:relay#autoOff"}
Switch Shelly_XXXXX3_Relay        "Garage Light"                  {channel="shelly:shelly1:XXXXX3:relay#output"}
Switch Shelly_XXXXX3_Input        "Garage Switch (Input)"         {channel="shelly:shelly1:XXXXX3:relay#input"}

/* Sensors */
Number ShellyHT_Dormitorio_Temp  "Dormitorio Temperature" <temperature> {channel="shelly:shellyht:e01681:sensors#temperature"}
Number ShellyHT_Dormitorio_Humid "Dormitorio Humidity"    <humidity>    {channel="shelly:shellyht:e01681:sensors#humidity"}
Number ShellyHT_Dormitorio_Batt  "Dormitorio Battery"     <battery>     {channel="shelly:shellyht:e01681:battery#batteryLevel"}
Number ShellyHT_Chimenea_Temp    "Chimenea Temperature"   <temperature> {channel="shelly:shellyht:e01691:sensors#temperature"}
Number ShellyHT_Chimenea_Humid   "Chimenea Humidity"      <humidity>    {channel="shelly:shellyht:e01691:sensors#humidity"}
Number ShellyHT_Chimenea_Batt    "Chimenea Battery"       <battery>     {channel="shelly:shellyht:e01691:battery#batteryLevel"}
Number ShellyF_Sotano_Temp       "Sotano Temperature"     <temperature> {channel="shelly:shellyflood:764fe0:sensors#temperature"}
Number ShellyF_Sotano_Batt       "Sotano Battery"         <battery>     {channel="shelly:shellyflood:764fe0:battery#batteryLevel"}
Switch ShellyF_Sotano_Flood      "Sotano Flood Alarm"     <alarm>       {channel="shelly:shellyflood:764fe0:sensors#flood"}

/* Dimmer */
Switch DimmerSwitch     "Light on/off"                       {channel="shelly:shellydimmer:XXX:relay#brightness"}
Dimmer DimmerBrightness "Garage Light Brightness"            {channel="shelly:shellydimmer:XXX:relay#brightness"}
Dimmer DimmerIncDec     "Garage Light +/-"                   {channel="shelly:shellydimmer:XXX:relay#brightness"}

Number Shelly_Power     "Bath Room Light Power"                {channel="shelly:shelly1:XXXXXX:meter#currentWatts"} /* Power Meter */

```

### shelly.rules

#### Catch alarms

```
rule "Monitor Shelly Restart"
when
    Channel "shelly:shelly2-relay:XXXXXX:device#alarm" triggered OVERTEMP
then
        logInfo("Shelly1", "Device is getting to hot!!")
end
```

#### Trigger scene with Button-1

```
rule "Button-1 SHORT_PRESSED"
when
    Channel "shelly:shellybutton1:d8f15bXXXXXX:status#button" triggered SHORT_PRESSED
then
    logInfo("Button", "Shelly Button reported SHORT_PRESSED")
    if (MyTV.state != OFF) {
        logInfo("Button", "   switch TV OFF")
        sendCommand(MyTV, "OFF")
    } else {
        logInfo("Button", "   switch TV to ON")
        sendCommand(MyTV, ON)
    }
end

rule "Button-1 TRIPLE_PRESSED"
when
    Channel "shelly:shellybutton1:d8f15bXXXXXX:status#button" triggered TRIPLE_PRESSED
then
    logInfo("Button", "Shelly Button reported TRIPLE_PRESSED")
end
```

#### Observe battery status

pre-requisites:

- Install Send Mail Action
- Define a group called gBatteries
'Group   gBattery        "Batterien"         <battery>       (All)'
- Link battery channel for all your Shelly battery powered devices
- Add battery items to group gBattery

```
val String mailTo     = "alarm@openhab.me"

/* ------------- Battery Monitor ----------- */

rule "Battery Monitor"
when
    System started or
    Time cron "0 0 10 * * ?"
then
    logInfo("BatteryMon", "Check Battery state")

    if (! gBattery.allMembers.filter([state < lowBatteryThreshold]).empty) {
        message = "Battery levels:\n"

        var report = gBattery.allMembers.filter([ state instanceof DecimalType ]).sortBy([ state instanceof DecimalType ]).map[ 
        name + ": " + state.format("%d%%\n") ]
        message = message + report

        message = message + "\nBattery Level:\n"
        gBattery?.allMembers.forEach([sw|
            message = message + sw.name + ": " + state.format("%d%%\n")
        ])

        sendMail(mailTo, "Home: LOW Battery Alert!", message)
    }
    logInfo("BatteryMon", "Batteries checked.")
end
```

#### Control CCT LED stripes

Usage & Requirements:

- 4 Items per Thing required. Example:

```
Group gCCT_LED        "All CCT LEDs"
Dimmer    LED1_brightness     "Brightness"    (gCCT_LED)
Dimmer    LED1_temperature    "Temperature"   (gCCT_LED)
Dimmer    LED1_cw         "cold white Channel"
Dimmer    LED1_ww         "warm white Channel"
```

- Items "LED1" and "LED1_temperature" are proxy items and to be used in sitemaps. Both have to be a member of group "gCCT_LED"
- Items "LED1_cw" and "LED_ww" are items linked to Thing channel. Not required in sitemaps. Do NOT include in this group
- Prefix: needs to be constant per Thing.

```
val String strSuffixSeparator = "_"             //Separator: 1 unique separator
val String strSuffixBrightness = "brightness"           //Suffix: at your choice
val String strSuffixTemperature = "temperature"         //Suffix: at your choice

rule "CCT_LED"
when
    Member of gCCT_LED changed
then
    logInfo("CCT_LED", "Item '{}' received command {}",triggeringItem.name,triggeringItem.state)
    var Number iNewCwState     //New value for cold white channel
    var Number iNewWwState     //New value for warm white channel
    val String strThing = triggeringItem.name.toString.split(strSuffixSeparator).get(0)     //Get Name of "Thing", i.e. LED1 or LED2 or...
    val String strType = triggeringItem.name.toString.split(strSuffixSeparator).get(1)      //Get Type (brightness oder temperature)

    if ((strType == strSuffixBrightness) && (triggeringItem.state as Number) == 0) {        //no math required. just switch off
        iNewCwState = 0
        iNewWwState = 0
    }
    else {
        var iBrightness = gCCT_LED.members.findFirst[ t | t.name == strThing+strSuffixSeparator+strSuffixBrightness ].state as Number
        var iColor = gCCT_LED.members.findFirst[ t | t.name == strThing+strSuffixSeparator+strSuffixTemperature ].state as Number
        logInfo("CCT_LED", "Setting 'Brightness' to {} and 'White Color' to {}",iBrightness,iColor)
        iNewWwState = Math::round ((iColor / 100 * iBrightness).intValue)
        iNewCwState = iBrightness - iNewWwState
    }
    logInfo("CCT_LED", "Changing channel 'Cold White' to {} and 'Warm White' to {}",iNewCwState,iNewWwState)
    sendCommand(strThing + "_cw", iNewCwState.toString)
    sendCommand(strThing + "_ww", iNewWwState.toString)
end
```

#### Reading colors from Color Picker:

```
import org.openhab.core.library.types.*

rule "Get input change from garage light"
when
    Item Shelly_XXXXX3_Input changed to ON
then
    logInfo("Garage", "Light input is ON")
    BackDoorLight.sendCommand(ON)
end

rule "Momentary Switch events"
when
    Channel "shelly:shellydevice:XXXXXX:relay1#button" triggered SHORT_PRESSED
then
    logInfo("Relay", "A short push was detected")
end


rule "Shelly alarms"
when
    Channel "shelly:shellydevice:XXXXXX:device#alarm"       triggered or
    Channel "shelly:shelly25-roller:XXXXXX:device#alarm"    triggered
then
    if (receivedEvent !== null) { // A (channel) event triggered the rule
        eventSource = triggeredChannel
        eventType = receivedEvent
        ...
    } 
end

rule "Color changed" 
when
    Item ShellyColor changed
then
    var HSBType hsbValue = ShellyColor.state as HSBType
    var int redValue = hsbValue.red.intValue
    var int greenValue = hsbValue.green.intValue
    var int blueValue = hsbValue.blue.intValue
end

```

### shelly.sitemap

```
sitemap demo label="Home"
{
        Frame label="Dimmer" {
            Switch   item=DimmerSwitch
            Slider   item=DimmerBrightness
            SetPoint item=DimmerIncDec

            Number   item=ShellyHT_Dormitorio_Temp
            Number   item=ShellyHT_Chimenea_Humid
            Number   item=ShellyF_Sotano_Batt
            Number   item=Shelly_Power
        }
}
```
