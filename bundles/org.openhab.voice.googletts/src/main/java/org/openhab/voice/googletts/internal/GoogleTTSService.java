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
package org.openhab.voice.googletts.internal;

import static org.openhab.voice.googletts.internal.GoogleTTSService.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.googletts.internal.protocol.AudioEncoding;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class GoogleTTSService implements TTSService {
    /**
     * Service name
     */
    static final String SERVICE_NAME = "Google Cloud";

    /**
     * Service id
     */
    static final String SERVICE_ID = "googletts";

    /**
     * Service category
     */
    static final String SERVICE_CATEGORY = "voice";

    /**
     * Service pid
     */
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;

    /**
     * Cache folder under $userdata
     */
    private static final String CACHE_FOLDER_NAME = "cache";

    /**
     * Configuration parameters
     */
    private static final String PARAM_CLIENT_ID = "clientId";
    private static final String PARAM_CLIEND_SECRET = "clientSecret";
    static final String PARAM_AUTHCODE = "authcode";
    private static final String PARAM_PITCH = "pitch";
    private static final String PARAM_SPEAKING_RATE = "speakingRate";
    private static final String PARAM_VOLUME_GAIN_DB = "volumeGainDb";
    private static final String PARAM_PURGE_CACHE = "purgeCache";

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(GoogleTTSService.class);

    /**
     * Set of supported audio formats
     */
    private Set<AudioFormat> audioFormats = new HashSet<>();

    /**
     * Google Cloud TTS API implementation
     */
    private @NonNullByDefault({}) GoogleCloudAPI apiImpl;
    private final ConfigurationAdmin configAdmin;
    private final OAuthFactory oAuthFactory;

    /**
     * All voices for all supported locales
     */
    private Set<Voice> allVoices = new HashSet<>();

    private final GoogleTTSConfig config = new GoogleTTSConfig();

    @Activate
    public GoogleTTSService(final @Reference ConfigurationAdmin configAdmin,
            final @Reference OAuthFactory oAuthFactory) {
        this.configAdmin = configAdmin;
        this.oAuthFactory = oAuthFactory;
    }

    /**
     * DS activate, with access to ConfigAdmin
     */
    @Activate
    protected void activate(Map<String, Object> config) {
        // create cache folder
        File userData = new File(OpenHAB.getUserDataFolder());
        File cacheFolder = new File(new File(userData, CACHE_FOLDER_NAME), SERVICE_PID);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        logger.debug("Using cache folder {}", cacheFolder.getAbsolutePath());

        apiImpl = new GoogleCloudAPI(configAdmin, oAuthFactory, cacheFolder);
        updateConfig(config);
    }

    /**
     * Initializing audio formats. Google supports 3 formats:
     * LINEAR16
     * Uncompressed 16-bit signed little-endian samples (Linear PCM). Audio content returned as LINEAR16
     * also contains a WAV header.
     * MP3
     * MP3 audio.
     * OGG_OPUS
     * Opus encoded audio wrapped in an ogg container. This is not supported by openHAB.
     *
     * @return Set of supported AudioFormats
     */
    private Set<AudioFormat> initAudioFormats() {
        logger.trace("Initializing audio formats");
        Set<AudioFormat> result = new HashSet<>();
        for (String format : apiImpl.getSupportedAudioFormats()) {
            AudioFormat audioFormat = getAudioFormat(format);
            if (audioFormat != null) {
                result.add(audioFormat);
                logger.trace("Audio format supported: {}", format);
            } else {
                logger.trace("Audio format not supported: {}", format);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Loads available voices from Google API
     *
     * @return Set of available voices.
     */
    private Set<Voice> initVoices() {
        logger.trace("Initializing voices");
        Set<Voice> result = new HashSet<>();
        for (Locale locale : apiImpl.getSupportedLocales()) {
            result.addAll(apiImpl.getVoicesForLocale(locale));
        }
        if (logger.isTraceEnabled()) {
            for (Voice voice : result) {
                logger.trace("Google Cloud TTS voice: {}", voice.getLabel());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Called by the framework when the configuration was updated.
     *
     * @param newConfig Updated configuration
     */
    @Modified
    private void updateConfig(Map<String, Object> newConfig) {
        logger.debug("Updating configuration");
        if (newConfig != null) {
            // client id
            String param = newConfig.containsKey(PARAM_CLIENT_ID) ? newConfig.get(PARAM_CLIENT_ID).toString() : null;
            config.clientId = param;
            if (param == null) {
                logger.warn("Missing client id configuration to access Google Cloud TTS API.");
            }
            // client secret
            param = newConfig.containsKey(PARAM_CLIEND_SECRET) ? newConfig.get(PARAM_CLIEND_SECRET).toString() : null;
            config.clientSecret = param;
            if (param == null) {
                logger.warn("Missing client secret configuration to access Google Cloud TTS API.");
            }
            // authcode
            param = newConfig.containsKey(PARAM_AUTHCODE) ? newConfig.get(PARAM_AUTHCODE).toString() : null;
            config.authcode = param;

            // pitch
            param = newConfig.containsKey(PARAM_PITCH) ? newConfig.get(PARAM_PITCH).toString() : null;
            if (param != null) {
                config.pitch = Double.parseDouble(param);
            }

            // speakingRate
            param = newConfig.containsKey(PARAM_SPEAKING_RATE) ? newConfig.get(PARAM_SPEAKING_RATE).toString() : null;
            if (param != null) {
                config.speakingRate = Double.parseDouble(param);
            }

            // volumeGainDb
            param = newConfig.containsKey(PARAM_VOLUME_GAIN_DB) ? newConfig.get(PARAM_VOLUME_GAIN_DB).toString() : null;
            if (param != null) {
                config.volumeGainDb = Double.parseDouble(param);
            }

            // purgeCache
            param = newConfig.containsKey(PARAM_PURGE_CACHE) ? newConfig.get(PARAM_PURGE_CACHE).toString() : null;
            if (param != null) {
                config.purgeCache = Boolean.parseBoolean(param);
            }
            logger.trace("New configuration: {}", config.toString());

            if (config.clientId != null && !config.clientId.isEmpty() && config.clientSecret != null
                    && !config.clientSecret.isEmpty()) {
                apiImpl.setConfig(config);
                if (apiImpl.isInitialized()) {
                    allVoices = initVoices();
                    audioFormats = initAudioFormats();
                }
            }
        } else {
            logger.warn("Missing Google Cloud TTS configuration.");
        }
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        return allVoices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return audioFormats;
    }

    /**
     * Helper to create AudioFormat objects from Google names.
     *
     * @param format Google audio format.
     * @return Audio format object.
     */
    private @Nullable AudioFormat getAudioFormat(String format) {
        Integer bitDepth = 16;
        Long frequency = 44100L;

        AudioEncoding encoding = AudioEncoding.valueOf(format);

        switch (encoding) {
            case MP3:
                // we use by default: MP3, 44khz_16bit_mono with bitrate 64 kbps
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, bitDepth, 64000,
                        frequency);
            case LINEAR16:
                // we use by default: wav, 44khz_16bit_mono
                return new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, bitDepth, null,
                        frequency);
            default:
                logger.warn("Audio format {} is not yet supported.", format);
                return null;
        }
    }

    /**
     * Checks parameters and calls the API to synthesize voice.
     *
     * @param text Input text.
     * @param voice Selected voice.
     * @param requestedFormat Format that is supported by the target sink as well.
     * @return Output audio stream
     * @throws TTSException in case the service is unavailable or a parameter is invalid.
     */
    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate known api key
        if (!apiImpl.isInitialized()) {
            throw new TTSException("Missing service configuration.");
        }
        // Validate arguments
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!this.allVoices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(requestedFormat)) {
                isAudioFormatSupported = true;
                break;
            }
        }
        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        // create the audio byte array for given text, locale, format
        byte[] audio = apiImpl.synthesizeSpeech(trimmedText, (GoogleTTSVoice) voice, requestedFormat.getCodec());
        if (audio == null) {
            throw new TTSException("Could not read from Google Cloud TTS Service");
        }
        return new ByteArrayAudioStream(audio, requestedFormat);
    }
}
