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
package org.openhab.voice.pollytts.internal;

import java.io.File;

import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;

/**
 * Implementation of the {@link AudioStream} interface for the {@link PollyTTSService}.
 * It simply uses a {@link FileAudioStream} which is doing all the necessary work,
 * e.g. supporting MP3 and WAV files with fixed stream length.
 *
 * @author Robert Hillman - Initial contribution
 */
class PollyTTSAudioStream extends FileAudioStream {

    /**
     * main method the passes the audio file to system audio services
     */
    public PollyTTSAudioStream(File audioFile, AudioFormat format) throws AudioException {
        super(audioFile, format);
    }
}
