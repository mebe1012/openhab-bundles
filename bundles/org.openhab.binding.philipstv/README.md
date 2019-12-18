# <bindingName> Binding

Philips TV Binding for 2016+ Series, which use the Android TV OS and Jointspace v6+.
## Supported Things

Only Philips TVs produced in the year 2016 or later, using the Android TV OS.

## Discovery

The TV must be turned on in order to be discovered. It will be initial named after its TV model description.
 
Afterwards the one-time pairing process must be worked through.

## Thing Configuration


You need to pair your TV first, in order to control it:

1.  As soon as you add the discovered TV in your inbox as a Thing, the pairing process will start.
2.  A Pairing Code will be presented on your TV.
3.  Go to the created Thing (in PaperUI: Configuration > Things) and set the Pairing Code as a Configuration Parameter.
4.  Your TV is now fully controllable.

Additional Configuration is possible, but not a must:

| Configuration  | Type | Description                                                                                             |
|------------------|-----------|---------------------------------------------------------------------------------------------------------|
| Network Address  | Text    | IP Address of the TV. Automatically resolved with UPnP Discovery.                                                                                 |
| Network Port     | Integer   | Used Port for the TV. Defaults to 1926.                                                                                |
| MAC Address      | Text      | MAC address of the Philips TV device. |
| Pairing Code     | Text      | Needed for first time retrieval of credentials. See instructions above.                                                                        |
| Refresh Rate     | Integer   | Refreshes TV status details. If UPnP Discovery is turned off, this determines how often the power state of the tv is checked.                                                                            |
| Username         | Text    | Username for the authentication against the Philips TV.  
| Password         | Text    | Password for the authentication against the Philips TV. |     
| Use UPnP Discovery | Boolean    | Enables UPnP Discovery. If disabled, constant HTTPS polling will happen. Defaults to true, which is the recommended way. |  

## Channels

TVs support the following channels:

| Channel Type ID  | Item Type | Description                                                                                             |
|------------------|-----------|---------------------------------------------------------------------------------------------------------|
| volume           | Number    | Volume level of the TV.                                                                                 |
| mute             | Switch    | Mute state of the TV.                                                                                   |
| AppName          | String    | Name of the current running App. Changing this to a value from the available App List in this Item, starts an Application.                                                                             |
| AppIcon          | Image     | Icon of the current running App.                                                                               |
| tvChannel        | String    | Name of the current TV channel. Changing this to a value from the available TV Channel List in this Item, starts a TV Channel.                                                                             |
| player           | Player    | Player item which emulates the infrared remote controller actions.                                                         |
| searchContent    | String    | Changing this value toggles the Google Assistant search on the TV for the given input.                                                           |
| power            | Switch    | TV power. Turning on only works in combination with Wake-On-LAN.                        |
| keyCode          | String    | The key code channel emulates the infrared remote controller and allows to send virtual button presses. |
| ambilightPower   | Switch    | Ambilight power control.                        | 
| ambilightHuePower| Switch    | Ambilight + Hue power control.                        |
| ambilightStyle   | String    | Ambilight Style plus Algorithm used, e.g. "FOLLOW_VIDEO STANDARD" or "FOLLOW_COLOR HOT_LAVA".           |
| ambilightColor   | Color     | Color for all Ambilight Sides. Changing this color will affect all sides at once.                       |
| ambilightLeftColor| Color    | Color for left Ambilight Side. Changing this color will affect the left side.                           |
| ambilightRightColor| Color   | Color for right Ambilight Side. Changing this color will affect the right side.                         |
| ambilightTopColor| Color     | Color for top Ambilight Side. Changing this color will affect the top side.                             |
| ambilightBottomColor| Color  | Color for bottom Ambilight Side. Changing this color will affect the bottom side.                       |
| brightness       | Dimmer    | Brightness of the TV picture.                                                                           |
| contrast         | Dimmer    | Contrast of the TV picture.                                                                             |
| sharpness        | Dimmer    | Sharpness of the TV picture.                                                                            |
| volume           | Number    | Volume level of the TV.                                                                                 |
## Full Example

```
Number PhilipsTV_Volume "Philips TV - Volume" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:volume"}
Switch PhilipsTV_Power "Philips TV - Power" (channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:power"}
Switch PhilipsTV_Mute "Philips TV - Mute" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:mute"}
String PhilipsTV_Key_Code "Philips TV - Key Code emulation" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:keyCode"}
String PhilipsTV_App_Name "Philips TV - Current App" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:appName"}
Image  PhilipsTV_App_Icon "Philips TV - Current App Icon" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:appIcon"}
String PhilipsTV_Channel_Name "Philips TV - Current Channel" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:tvChannel"}
Player PhilipsTV_Player "Philips TV - Player" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:player"}
String PhilipsTV_Search_Content "Philips TV - Search Content" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:searchContent"}
Switch PhilipsTV_Ambilight_Power "Philips TV - Ambilight Power" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:ambilightPower"}
Switch PhilipsTV_Ambilight_Hue_Power "Philips TV - Ambilight Hue Power" {channel="philipstv:tv:5AFEF00D_BABE_DADA_FA5A_1c5a6bef9271:ambilightHuePower"}
Switch PhilipsTV_Ambilight_Lounger_Power "Philips TV - Ambilight Lounge Power" (Fernseher) {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightLoungePower"}
String PhilipsTV_Ambilight_Style "Philips TV - Ambilight Style" (Fernseher) {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightStyle"}
Color PhilipsTv_AmbilightAllColor "Philips TV - All Sides Ambilight Color" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightColor"}
Color PhilipsTv_AmbilightLeftColor "Philips TV - Left Side Ambilight Color" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightLeftColor"}
Color PhilipsTv_AmbilightRightColor "Philips TV - Right Side Ambilight Color" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightRightColor"}
Color PhilipsTv_AmbilightTopColor "Philips TV - Top Side Ambilight Color" (Fernseher) {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightTopColor"}
Color PhilipsTv_AmbilightBottomColor "Philips TV - Bottom Side Ambilight Color" (Fernseher) {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:ambilightBottomColor"}
Dimmer PhilipsTv_Brightness "Philips TV - Brightness" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:brightness"}
Dimmer PhilipsTv_Contrast "Philips TV - Contrast" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:contrast"}
Dimmer PhilipsTv_Sharpness "Philips TV - Sharpness" {channel="philipstv:tv:F00DBABE_AA5E_BABA_DADA_1c5a6bef9271:sharpness"}
```
