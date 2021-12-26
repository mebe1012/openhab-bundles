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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.voice.googletts.internal.protocol.AudioConfig;
import org.openhab.voice.googletts.internal.protocol.AudioEncoding;
import org.openhab.voice.googletts.internal.protocol.ListVoicesResponse;
import org.openhab.voice.googletts.internal.protocol.SsmlVoiceGender;
import org.openhab.voice.googletts.internal.protocol.SynthesisInput;
import org.openhab.voice.googletts.internal.protocol.SynthesizeSpeechRequest;
import org.openhab.voice.googletts.internal.protocol.SynthesizeSpeechResponse;
import org.openhab.voice.googletts.internal.protocol.Voice;
import org.openhab.voice.googletts.internal.protocol.VoiceSelectionParams;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Google Cloud TTS API call implementation.
 *
 * @author Gabor Bicskei - Initial contribution and API
 */
class GoogleCloudAPI {

    private static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    private static final String BEARER = "Bearer ";

    private static final String GCP_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private static final String GCP_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
    private static final String GCP_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    /**
     * Google Cloud Platform authorization scope
     */
    private static final String GCP_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    /**
     * URL used for retrieving the list of available voices
     */
    private static final String LIST_VOICES_URL = "https://texttospeech.googleapis.com/v1/voices";

    /**
     * URL used for synthesizing text to speech
     */
    private static final String SYTNHESIZE_SPEECH_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(GoogleCloudAPI.class);

    /**
     * Supported voices and locales
     */
    private final Map<Locale, Set<GoogleTTSVoice>> voices = new HashMap<>();

    /**
     * Cache folder
     */
    private File cacheFolder;

    /**
     * Configuration
     */
    private @Nullable GoogleTTSConfig config;

    /**
     * Status flag
     */
    private boolean initialized;

    private final Gson gson = new GsonBuilder().create();
    private final ConfigurationAdmin configAdmin;
    private final OAuthFactory oAuthFactory;

    private @Nullable OAuthClientService oAuthService;

    /**
     * Constructor.
     *
     * @param cacheFolder Service cache folder
     */
    GoogleCloudAPI(ConfigurationAdmin configAdmin, OAuthFactory oAuthFactory, File cacheFolder) {
        this.configAdmin = configAdmin;
        this.oAuthFactory = oAuthFactory;
        this.cacheFolder = cacheFolder;
    }

    /**
     * Configuration update.
     *
     * @param config New configuration.
     */
    void setConfig(GoogleTTSConfig config) {
        this.config = config;

        String clientId = config.clientId;
        String clientSecret = config.clientSecret;
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
            try {
                final OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(
                        GoogleTTSService.SERVICE_PID, GCP_TOKEN_URI, GCP_AUTH_URI, clientId, clientSecret, GCP_SCOPE,
                        false);
                this.oAuthService = oAuthService;
                getAccessToken();
                initialized = true;
                initVoices();
            } catch (AuthenticationException | IOException ex) {
                logger.warn("Error initializing Google Cloud TTS service: {}", ex.getMessage());
                oAuthService = null;
                initialized = false;
                voices.clear();
            }
        } else {
            oAuthService = null;
            initialized = false;
            voices.clear();
        }

        // maintain cache
        if (config.purgeCache) {
            File[] files = cacheFolder.listFiles();
            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(File::delete);
            }
            logger.debug("Cache purged.");
        }
    }

    /**
     * Fetches the OAuth2 tokens from Google Cloud Platform if the auth-code is set in the configuration. If successful
     * the auth-code will be removed from the configuration.
     */
    private void getAccessToken() throws AuthenticationException, IOException {
        String authcode = config.authcode;
        if (authcode != null && !authcode.isEmpty()) {
            logger.debug("Trying to get access and refresh tokens.");
            try {
                oAuthService.getAccessTokenResponseByAuthorizationCode(authcode, GCP_REDIRECT_URI);
            } catch (OAuthException | OAuthResponseException ex) {
                logger.debug("Error fetching access token: {}", ex.getMessage(), ex);
                throw new AuthenticationException(
                        "Error fetching access token. Invalid authcode? Please generate a new one.");
            }

            config.authcode = null;

            try {
                Configuration serviceConfig = configAdmin.getConfiguration(GoogleTTSService.SERVICE_PID);
                Dictionary<String, Object> configProperties = serviceConfig.getProperties();
                if (configProperties != null) {
                    configProperties.put(GoogleTTSService.PARAM_AUTHCODE, "");
                    serviceConfig.update(configProperties);
                }
            } catch (IOException e) {
                // should not happen
                logger.warn(
                        "Failed to update configuration for Google Cloud TTS service. Please clear the 'authcode' configuration parameter manualy.");
            }
        }
    }

    private String getAuthorizationHeader() throws AuthenticationException, IOException {
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException ex) {
            logger.debug("Error fetching access token: {}", ex.getMessage(), ex);
            throw new AuthenticationException(
                    "Error fetching access token. Invalid authcode? Please generate a new one.");
        }
        if (accessTokenResponse == null || accessTokenResponse.getAccessToken() == null
                || accessTokenResponse.getAccessToken().isEmpty()) {
            throw new AuthenticationException("No access token. Is this thing authorized?");
        }
        return BEARER + accessTokenResponse.getAccessToken();
    }

    /**
     * Loads supported audio formats
     *
     * @return Set of audio formats
     */
    Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<>();
        for (AudioEncoding audioEncoding : AudioEncoding.values()) {
            if (audioEncoding != AudioEncoding.AUDIO_ENCODING_UNSPECIFIED) {
                formats.add(audioEncoding.toString());
            }
        }
        return formats;
    }

    /**
     * Supported locales.
     *
     * @return Set of locales
     */
    Set<Locale> getSupportedLocales() {
        return voices.keySet();
    }

    /**
     * Supported voices for locale.
     *
     * @param locale Locale
     * @return Set of voices
     */
    Set<GoogleTTSVoice> getVoicesForLocale(Locale locale) {
        Set<GoogleTTSVoice> localeVoices = voices.get(locale);
        return localeVoices != null ? localeVoices : Collections.emptySet();
    }

    /**
     * Google API call to load locales and voices.
     */
    private void initVoices() throws AuthenticationException, IOException {
        if (oAuthService != null) {
            voices.clear();
            for (GoogleTTSVoice voice : listVoices()) {
                Locale locale = voice.getLocale();
                Set<GoogleTTSVoice> localeVoices;
                if (!voices.containsKey(locale)) {
                    localeVoices = new HashSet<>();
                    voices.put(locale, localeVoices);
                } else {
                    localeVoices = voices.get(locale);
                }
                localeVoices.add(voice);
            }
        } else {
            logger.error("Google client is not initialized!");
        }
    }

    @SuppressWarnings("null")
    private List<GoogleTTSVoice> listVoices() throws AuthenticationException, IOException {
        HttpRequestBuilder builder = HttpRequestBuilder.getFrom(LIST_VOICES_URL)
                .withHeader(HttpHeader.AUTHORIZATION.name(), getAuthorizationHeader());

        ListVoicesResponse listVoicesResponse = gson.fromJson(builder.getContentAsString(), ListVoicesResponse.class);

        if (listVoicesResponse == null || listVoicesResponse.getVoices() == null) {
            return Collections.emptyList();
        }

        List<GoogleTTSVoice> result = new ArrayList<>();
        for (Voice voice : listVoicesResponse.getVoices()) {
            for (String languageCode : voice.getLanguageCodes()) {
                result.add(new GoogleTTSVoice(Locale.forLanguageTag(languageCode), voice.getName(),
                        voice.getSsmlGender().name()));
            }
        }

        return result;
    }

    /**
     * Converts audio format to Google parameters.
     *
     * @param codec Requested codec
     * @return String array of Google audio format and the file extension to use.
     */
    private String[] getFormatForCodec(String codec) {
        switch (codec) {
            case AudioFormat.CODEC_MP3:
                return new String[] { AudioEncoding.MP3.toString(), "mp3" };
            case AudioFormat.CODEC_PCM_SIGNED:
                return new String[] { AudioEncoding.LINEAR16.toString(), "wav" };
            default:
                throw new IllegalArgumentException("Audio format " + codec + " is not yet supported");
        }
    }

    byte[] synthesizeSpeech(String text, GoogleTTSVoice voice, String codec) {
        String[] format = getFormatForCodec(codec);
        String fileNameInCache = getUniqueFilenameForText(text, voice.getTechnicalName());
        File audioFileInCache = new File(cacheFolder, fileNameInCache + "." + format[1]);
        try {
            // check if in cache
            if (audioFileInCache.exists()) {
                logger.debug("Audio file {} was found in cache.", audioFileInCache.getName());
                return Files.readAllBytes(audioFileInCache.toPath());
            }

            // if not in cache, get audio data and put to cache
            byte[] audio = synthesizeSpeechByGoogle(text, voice, format[0]);
            if (audio != null) {
                saveAudioAndTextToFile(text, audioFileInCache, audio, voice.getTechnicalName());
            }
            return audio;
        } catch (AuthenticationException ex) {
            logger.warn("Error initializing Google Cloud TTS service: {}", ex.getMessage());
            oAuthService = null;
            initialized = false;
            voices.clear();
            return null;
        } catch (FileNotFoundException ex) {
            logger.warn("Could not write {} to cache", audioFileInCache, ex);
            return null;
        } catch (IOException ex) {
            logger.error("Could not write {} to cache", audioFileInCache, ex);
            return null;
        }
    }

    /**
     * Create cache entry.
     *
     * @param text Converted text.
     * @param cacheFile Cache entry file.
     * @param audio Byte array of the audio.
     * @param voiceName Used voice
     * @throws IOException in case of file handling exceptions
     */
    private void saveAudioAndTextToFile(String text, File cacheFile, byte[] audio, String voiceName)
            throws IOException {
        logger.debug("Caching audio file {}", cacheFile.getName());
        try (FileOutputStream audioFileOutputStream = new FileOutputStream(cacheFile)) {
            audioFileOutputStream.write(audio);
        }

        // write text to file for transparency too
        // this allows to know which contents is in which audio file
        String textFileName = removeExtension(cacheFile.getName()) + ".txt";
        logger.debug("Caching text file {}", textFileName);
        try (FileOutputStream textFileOutputStream = new FileOutputStream(new File(cacheFolder, textFileName))) {
            // @formatter:off
            StringBuilder sb = new StringBuilder("Config: ")
                    .append(config.toConfigString())
                    .append(",voice=")
                    .append(voiceName)
                    .append(System.lineSeparator())
                    .append("Text: ")
                    .append(text)
                    .append(System.lineSeparator());
            // @formatter:on
            textFileOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Removes the extension of a file name.
     *
     * @param fileName the file name to remove the extension of
     * @return the filename without the extension
     */
    private String removeExtension(String fileName) {
        int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = Math.max(fileName.lastIndexOf(UNIX_SEPARATOR), fileName.lastIndexOf(WINDOWS_SEPARATOR));
        return lastSeparator > extensionPos ? fileName : fileName.substring(0, extensionPos);
    }

    /**
     * Call Google service to synthesize the required text
     *
     * @param text Text to synthesize
     * @param voice Voice parameter
     * @param audioFormat Audio encoding format
     * @return Audio input stream or {@code null} when encoding exceptions occur
     */
    @SuppressWarnings({ "null", "unused" })
    private byte[] synthesizeSpeechByGoogle(String text, GoogleTTSVoice voice, String audioFormat)
            throws AuthenticationException, IOException {
        AudioConfig audioConfig = new AudioConfig(AudioEncoding.valueOf(audioFormat), config.pitch, config.speakingRate,
                config.volumeGainDb);
        SynthesisInput synthesisInput = new SynthesisInput(text);
        VoiceSelectionParams voiceSelectionParams = new VoiceSelectionParams(voice.getLocale().getLanguage(),
                voice.getLabel(), SsmlVoiceGender.valueOf(voice.getSsmlGender()));

        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest(audioConfig, synthesisInput,
                voiceSelectionParams);

        HttpRequestBuilder builder = HttpRequestBuilder.postTo(SYTNHESIZE_SPEECH_URL)
                .withHeader(HttpHeader.AUTHORIZATION.name(), getAuthorizationHeader())
                .withContent(gson.toJson(request), MimeTypes.Type.APPLICATION_JSON.name());

        SynthesizeSpeechResponse synthesizeSpeechResponse = gson.fromJson(builder.getContentAsString(),
                SynthesizeSpeechResponse.class);

        if (synthesizeSpeechResponse == null) {
            return null;
        }

        byte[] encodedBytes = synthesizeSpeechResponse.getAudioContent().getBytes(StandardCharsets.UTF_8);
        return Base64.getDecoder().decode(encodedBytes);
    }

    /**
     * Gets a unique filename for a give text, by creating a MD5 hash of it. It
     * will be preceded by the locale.
     * <p>
     * Sample: "en-US_00a2653ac5f77063bc4ea2fee87318d3"
     */
    private String getUniqueFilenameForText(String text, String voiceName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = (config.toConfigString() + text).getBytes(StandardCharsets.UTF_8);
            String fileNameHash = String.format("%032x", new BigInteger(1, md.digest(bytesOfMessage)));
            return voiceName + "_" + fileNameHash;
        } catch (NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, ex);
            return null;
        }
    }

    boolean isInitialized() {
        return initialized;
    }
}
