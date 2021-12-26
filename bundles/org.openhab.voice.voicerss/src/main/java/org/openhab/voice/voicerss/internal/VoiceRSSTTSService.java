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
package org.openhab.voice.voicerss.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.voicerss.internal.cloudapi.CachedVoiceRSSCloudImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using VoiceRSS TTS service.
 *
 * @author Jochen Hiller - Initial contribution and API
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 */
@Component(configurationPid = "org.openhab.voicerss", property = Constants.SERVICE_PID + "=org.openhab.voicerss")
@ConfigurableService(category = "voice", label = "VoiceRSS Text-to-Speech", description_uri = "voice:voicerss")
public class VoiceRSSTTSService implements TTSService {

    /** Cache folder name is below userdata/voicerss/cache. */
    private static final String CACHE_FOLDER_NAME = "voicerss" + File.separator + "cache";

    // API Key comes from ConfigAdmin
    private static final String CONFIG_API_KEY = "apiKey";
    private String apiKey;

    private final Logger logger = LoggerFactory.getLogger(VoiceRSSTTSService.class);

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedVoiceRSSCloudImpl voiceRssImpl;

    /**
     * Set of supported voices
     */
    private Set<Voice> voices;

    /**
     * Set of supported audio formats
     */
    private Set<AudioFormat> audioFormats;

    /**
     * DS activate, with access to ConfigAdmin
     */
    protected void activate(Map<String, Object> config) {
        try {
            modified(config);
            voiceRssImpl = initVoiceImplementation();
            voices = initVoices();
            audioFormats = initAudioFormats();

            logger.debug("Using VoiceRSS cache folder {}", getCacheFolderName());
        } catch (IllegalStateException e) {
            logger.error("Failed to activate VoiceRSS: {}", e.getMessage(), e);
        }
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        if (config != null) {
            apiKey = config.containsKey(CONFIG_API_KEY) ? config.get(CONFIG_API_KEY).toString() : null;
        }
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        return Collections.unmodifiableSet(voices);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Collections.unmodifiableSet(audioFormats);
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate known api key
        if (apiKey == null) {
            throw new TTSException("Missing API key, configure it first before using");
        }
        // Validate arguments
        if (text == null) {
            throw new TTSException("The passed text is null");
        }
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is empty");
        }
        if (!voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat currentAudioFormat : audioFormats) {
            if (currentAudioFormat.isCompatible(requestedFormat)) {
                isAudioFormatSupported = true;
                break;
            }
        }
        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        // now create the input stream for given text, locale, format. There is
        // only a default voice
        try {
            File cacheAudioFile = voiceRssImpl.getTextToSpeechAsFile(apiKey, trimmedText,
                    voice.getLocale().toLanguageTag(), voice.getLabel(), getApiAudioFormat(requestedFormat));
            if (cacheAudioFile == null) {
                throw new TTSException("Could not read from VoiceRSS service");
            }
            return new VoiceRSSAudioStream(cacheAudioFile, requestedFormat);
        } catch (AudioException ex) {
            throw new TTSException("Could not create AudioStream: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new TTSException("Could not read from VoiceRSS service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Initializes voices.
     *
     * @return The voices of this instance
     */
    private Set<Voice> initVoices() {
        Set<Voice> voices = new HashSet<>();
        for (Locale locale : voiceRssImpl.getAvailableLocales()) {
            for (String voiceLabel : voiceRssImpl.getAvailableVoices(locale)) {
                voices.add(new VoiceRSSVoice(locale, voiceLabel));
            }
        }
        return voices;
    }

    /**
     * Initializes audioFormats
     *
     * @return The audio formats of this instance
     */
    private Set<AudioFormat> initAudioFormats() {
        Set<AudioFormat> audioFormats = new HashSet<>();
        for (String format : voiceRssImpl.getAvailableAudioFormats()) {
            audioFormats.add(getAudioFormat(format));
        }
        return audioFormats;
    }

    private AudioFormat getAudioFormat(String apiFormat) {
        Boolean bigEndian = null;
        Integer bitDepth = 16;
        Integer bitRate = null;
        Long frequency = 44100L;

        if ("MP3".equals(apiFormat)) {
            // we use by default: MP3, 44khz_16bit_mono with bitrate 64 kbps
            bitRate = 64000;
            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, bigEndian, bitDepth, bitRate,
                    frequency);
        } else if ("OGG".equals(apiFormat)) {
            // we use by default: OGG, 44khz_16bit_mono
            return new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, bigEndian, bitDepth, bitRate,
                    frequency);
        } else if ("AAC".equals(apiFormat)) {
            // we use by default: AAC, 44khz_16bit_mono
            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_AAC, bigEndian, bitDepth, bitRate,
                    frequency);
        } else {
            throw new IllegalArgumentException("Audio format " + apiFormat + " not yet supported");
        }
    }

    private String getApiAudioFormat(AudioFormat format) {
        if (format.getCodec().equals(AudioFormat.CODEC_MP3)) {
            return "MP3";
        } else if (format.getCodec().equals(AudioFormat.CODEC_VORBIS)) {
            return "OGG";
        } else if (format.getCodec().equals(AudioFormat.CODEC_AAC)) {
            return "AAC";
        } else {
            throw new IllegalArgumentException("Audio format " + format.getCodec() + " not yet supported");
        }
    }

    private CachedVoiceRSSCloudImpl initVoiceImplementation() {
        return new CachedVoiceRSSCloudImpl(getCacheFolderName());
    }

    private String getCacheFolderName() {
        // we assume that this folder does NOT have a trailing separator
        return OpenHAB.getUserDataFolder() + File.separator + CACHE_FOLDER_NAME;
    }

    @Override
    public String getId() {
        return "voicerss";
    }

    @Override
    public String getLabel(Locale locale) {
        return "VoiceRSS";
    }
}
