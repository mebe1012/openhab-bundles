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
4. Your TV is now fully controllable.

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
| searchContent    | String    | Changing this value toggles the Google Assisntant search on the TV for the given input.                                                           |
| power            | Switch    | TV power. Turning on only works in combination with Wake-On-LAN.                        |
| keyCode          | String    | The key code channel emulates the infrared remote controller and allows to send virtual button presses. |
| ambilightPower   | Switch    | Ambilight power control.                        | 
| ambilightHuePower| Switch    | Ambilight + Hue power control.                        |

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
```