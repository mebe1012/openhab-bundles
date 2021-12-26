# Google Cloud Text-to-Speech

Google Cloud TTS Service uses the non-free Google Cloud Text-to-Speech API to convert text or Speech Synthesis Markup Language (SSML) input into audio data of natural human speech. 
It provides multiple voices, available in different languages and variants and applies DeepMind’s groundbreaking research in WaveNet and Google’s powerful neural networks. 
The implementation caches the converted texts to reduce the load on the API and make the conversion faster.
You can find them in the `$OPENHAB_USERDATA/cache/org.openhab.voice.googletts` folder.
Be aware, that using this service may incur cost on your Google Cloud account.
You can find pricing information on the [documentation page](https://cloud.google.com/text-to-speech/#pricing-summary).

## Table of Contents

<!-- MarkdownTOC -->

* [Obtaining Credentials](#obtaining-credentials)
* [Service Configuration](#service-configuration)
* [Voice Configuration](#voice-configuration)

<!-- /MarkdownTOC -->

## Obtaining Credentials

Before you can integrate this service with your Google Cloud Text-to-Speech, you must have a Google API Console project:

* Select or create a GCP project. [link](https://console.cloud.google.com/cloud-resource-manager)
* Make sure that billing is enabled for your project. [link](https://cloud.google.com/billing/docs/how-to/modify-project)
* Enable the Cloud Text-to-Speech API. [link](https://console.cloud.google.com/apis/dashboard)
* Set up authentication:
  * Go to the "APIs & Services" -> "Credentials" page in the GCP Console and your project. [link](https://console.cloud.google.com/apis/credentials)
  * From the "Create credentials" drop-down list, select "OAuth client ID.
  * Select application type "TV and Limited Input" and enter a name into the "Name" field.
  * Click Create. A pop-up appears, showing your "client ID" and "client secret".

## Service Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Text-to-Speech** and set:

* **Client Id** - Google Cloud Platform OAuth 2.0-Client Id.
* **Client Secret** - Google Cloud Platform OAuth 2.0-Client Secret.
* **Authorization Code** - The auth-code is a one-time code needed to retrieve the necessary access-codes from Google Cloud Platform.
**Please go to your browser ...**
[https://accounts.google.com/o/oauth2/auth?client_id=<clientId>&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/cloud-platform&response_type=code](https://accounts.google.com/o/oauth2/auth?client_id=<clientId>&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/cloud-platform&response_type=code) (replace `<clientId>` by your Client Id)
**... to generate an auth-code and paste it here**.
After initial authorization, this code is not needed anymore.
It is recommended to clear this configuration parameter afterwards.
* **Pitch** - The pitch of selected voice, up to 20 semitones.
* **Volume Gain** - The volume of the output between 16dB and -96dB.
* **Speaking Rate** - The speaking rate can be 4x faster or slower than the normal rate.
* **Purge Cache** - Purges the cache e.g. after testing different voice configuration parameters.

When enabled the cache is purged once.
Make sure to disable this setting again so the cache is maintained after restarts.

## Voice Configuration

Using your favorite configuration UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Google Cloud** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.
