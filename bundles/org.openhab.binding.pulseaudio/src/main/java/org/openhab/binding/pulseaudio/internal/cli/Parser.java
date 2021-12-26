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
package org.openhab.binding.pulseaudio.internal.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.pulseaudio.internal.PulseaudioClient;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.Module;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.openhab.binding.pulseaudio.internal.items.Source;
import org.openhab.binding.pulseaudio.internal.items.SourceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parsers for the pulseaudio return strings
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private static final Pattern PATTERN = Pattern.compile("^\\s*([a-z\\s._]+)[:=]\\s*<?\\\"?([^>\\\"]+)\\\"?>?$");
    private static final Pattern VOLUME_PATTERN = Pattern
            .compile("^([\\w\\-]+):( *[\\d]+ \\/)? *([\\d]+)% *\\/? *([\\d\\-., dB]+)?$");
    private static final Pattern FALL_BACK_PATTERN = Pattern
            .compile("^([0-9]+)([a-z\\s._]+)[:=]\\s*<?\"?([^>\"]+)\"?>?$");
    private static final Pattern NUMBER_VALUE_PATTERN = Pattern.compile("^([0-9]+).*$");

    /**
     * parses the pulseaudio servers answer to the list-modules command and returns a list of
     * {@link Module} objects
     *
     * @param raw the given string from the pulseaudio server
     * @return list of modules
     */
    public static List<Module> parseModules(String raw) {
        List<Module> modules = new ArrayList<>();
        String[] parts = raw.split("index: ");
        if (parts.length <= 1) {
            return modules;
        }
        // skip first part
        for (int i = 1; i < parts.length; i++) {
            String[] lines = parts[i].split("\n");
            Hashtable<String, String> properties = new Hashtable<>();
            int id = 0;
            try {
                id = Integer.valueOf(lines[0].trim());
            } catch (NumberFormatException e) {
                // sometime the line feed is missing here
                Matcher matcher = FALL_BACK_PATTERN.matcher(lines[0].trim());
                if (matcher.find()) {
                    id = Integer.valueOf(matcher.group(1));
                    properties.put(matcher.group(2).trim(), matcher.group(3).trim());
                }
            }
            for (int j = 1; j < lines.length; j++) {
                Matcher matcher = PATTERN.matcher(lines[j]);
                if (matcher.find()) {
                    properties.put(matcher.group(1).trim(), matcher.group(2).trim());
                }
            }
            if (properties.containsKey("name")) {
                Module module = new Module(id, properties.get("name"));
                if (properties.containsKey("argument")) {
                    module.setArgument(properties.get("argument"));
                }
                modules.add(module);
            }
        }
        return modules;
    }

    /**
     * parses the pulseaudio servers answer to the list-sinks command and returns a list of
     * {@link Sink} objects
     *
     * @param raw the given string from the pulseaudio server
     * @return list of sinks
     */
    public static Collection<Sink> parseSinks(String raw, PulseaudioClient client) {
        Hashtable<String, Sink> sinks = new Hashtable<>();
        String[] parts = raw.split("index: ");
        if (parts.length <= 1) {
            return sinks.values();
        }
        // skip first part
        List<Sink> combinedSinks = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String[] lines = parts[i].split("\n");
            Hashtable<String, String> properties = new Hashtable<>();
            int id = 0;
            try {
                id = Integer.valueOf(lines[0].trim());
            } catch (NumberFormatException e) {
                // sometime the line feed is missing here
                Matcher matcher = FALL_BACK_PATTERN.matcher(lines[0].trim());
                if (matcher.find()) {
                    id = Integer.valueOf(matcher.group(1));
                    properties.put(matcher.group(2).trim(), matcher.group(3).trim());
                }
            }
            for (int j = 1; j < lines.length; j++) {
                Matcher matcher = PATTERN.matcher(lines[j]);
                if (matcher.find()) {
                    properties.put(matcher.group(1).trim(), matcher.group(2).trim());
                }
            }
            if (properties.containsKey("name")) {
                Sink sink = new Sink(id, properties.get("name"),
                        client.getModule(getNumberValue(properties.get("module"))));
                if (properties.containsKey("state")) {
                    try {
                        sink.setState(AbstractAudioDeviceConfig.State.valueOf(properties.get("state")));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("unhandled state {} in sink item #{}", properties.get("state"), id);
                    }
                }
                if (properties.containsKey("muted")) {
                    sink.setMuted("yes".equalsIgnoreCase(properties.get("muted")));
                }
                if (properties.containsKey("volume")) {
                    sink.setVolume(Integer.valueOf(parseVolume(properties.get("volume"))));
                }
                if (properties.containsKey("combine.slaves")) {
                    // this is a combined sink, the combined sink object should be
                    String sinkNames = properties.get("combine.slaves");
                    if (sinkNames != null) {
                        for (String sinkName : sinkNames.replace("\"", "").split(",")) {
                            sink.addCombinedSinkName(sinkName);
                        }
                    }
                    combinedSinks.add(sink);
                }
                sinks.put(sink.getUIDName(), sink);
            }
        }
        for (Sink combinedSink : combinedSinks) {
            for (String sinkName : combinedSink.getCombinedSinkNames()) {
                combinedSink.addCombinedSink(sinks.get(sinkName));
            }
        }
        return sinks.values();
    }

    /**
     * parses the pulseaudio servers answer to the list-sink-inputs command and returns a list of
     * {@link SinkInput} objects
     *
     * @param raw the given string from the pulseaudio server
     * @return list of sink-inputs
     */
    public static List<SinkInput> parseSinkInputs(String raw, PulseaudioClient client) {
        List<SinkInput> items = new ArrayList<>();
        String[] parts = raw.split("index: ");
        if (parts.length <= 1) {
            return items;
        }
        for (int i = 1; i < parts.length; i++) {
            String[] lines = parts[i].split("\n");
            Hashtable<String, String> properties = new Hashtable<>();
            int id = 0;
            try {
                id = Integer.valueOf(lines[0].trim());
            } catch (NumberFormatException e) {
                // sometime the line feed is missing here
                Matcher matcher = FALL_BACK_PATTERN.matcher(lines[0].trim());
                if (matcher.find()) {
                    id = Integer.valueOf(matcher.group(1));
                    properties.put(matcher.group(2).trim(), matcher.group(3).trim());
                }
            }
            for (int j = 1; j < lines.length; j++) {
                Matcher matcher = PATTERN.matcher(lines[j]);
                if (matcher.find()) {
                    properties.put(matcher.group(1).trim(), matcher.group(2).trim());
                }
            }
            if (properties.containsKey("sink")) {
                String name = properties.containsKey("media.name") ? properties.get("media.name")
                        : properties.get("sink");
                SinkInput item = new SinkInput(id, name, client.getModule(getNumberValue(properties.get("module"))));
                if (properties.containsKey("state")) {
                    try {
                        item.setState(AbstractAudioDeviceConfig.State.valueOf(properties.get("state")));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("unhandled state {} in sink-input item #{}", properties.get("state"), id);
                    }
                }
                if (properties.containsKey("muted")) {
                    item.setMuted("yes".equalsIgnoreCase(properties.get("muted")));
                }
                if (properties.containsKey("volume")) {
                    item.setVolume(Integer.valueOf(parseVolume(properties.get("volume"))));
                }
                if (properties.containsKey("sink")) {
                    item.setSink(client.getSink(Integer.valueOf(getNumberValue(properties.get("sink")))));
                }
                items.add(item);
            }
        }
        return items;
    }

    /**
     * parses the pulseaudio servers answer to the list-sources command and returns a list of
     * {@link Source} objects
     *
     * @param raw the given string from the pulseaudio server
     * @return list of sources
     */
    public static List<Source> parseSources(String raw, PulseaudioClient client) {
        List<Source> sources = new ArrayList<>();
        String[] parts = raw.split("index: ");
        if (parts.length <= 1) {
            return sources;
        }
        // skip first part
        for (int i = 1; i < parts.length; i++) {
            String[] lines = parts[i].split("\n");
            Hashtable<String, String> properties = new Hashtable<>();
            int id = 0;
            try {
                id = Integer.valueOf(lines[0].trim());
            } catch (NumberFormatException e) {
                // sometime the line feed is missing here
                Matcher matcher = FALL_BACK_PATTERN.matcher(lines[0].trim());
                if (matcher.find()) {
                    id = Integer.valueOf(matcher.group(1));
                    properties.put(matcher.group(2).trim(), matcher.group(3).trim());
                }
            }
            for (int j = 1; j < lines.length; j++) {
                Matcher matcher = PATTERN.matcher(lines[j]);
                if (matcher.find()) {
                    properties.put(matcher.group(1).trim(), matcher.group(2).trim());
                }
            }
            if (properties.containsKey("name")) {
                Source source = new Source(id, properties.get("name"),
                        client.getModule(getNumberValue(properties.get("module"))));
                if (properties.containsKey("state")) {
                    try {
                        source.setState(AbstractAudioDeviceConfig.State.valueOf(properties.get("state")));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("unhandled state {} in source item #{}", properties.get("state"), id);
                    }
                }
                if (properties.containsKey("muted")) {
                    source.setMuted("yes".equalsIgnoreCase(properties.get("muted")));
                }
                if (properties.containsKey("volume")) {
                    source.setVolume(parseVolume(properties.get("volume")));
                }
                String monitorOf = properties.get("monitor_of");
                if (monitorOf != null) {
                    source.setMonitorOf(client.getSink(Integer.valueOf(monitorOf)));
                }
                sources.add(source);
            }
        }
        return sources;
    }

    /**
     * parses the pulseaudio servers answer to the list-source-outputs command and returns a list of
     * {@link SourceOutput} objects
     *
     * @param raw the given string from the pulseaudio server
     * @return list of source-outputs
     */
    public static List<SourceOutput> parseSourceOutputs(String raw, PulseaudioClient client) {
        List<SourceOutput> items = new ArrayList<>();
        String[] parts = raw.split("index: ");
        if (parts.length <= 1) {
            return items;
        }
        // skip first part
        for (int i = 1; i < parts.length; i++) {
            String[] lines = parts[i].split("\n");
            Hashtable<String, String> properties = new Hashtable<>();
            int id = 0;
            try {
                id = Integer.valueOf(lines[0].trim());
            } catch (NumberFormatException e) {
                // sometime the line feed is missing here
                Matcher matcher = FALL_BACK_PATTERN.matcher(lines[0].trim());
                if (matcher.find()) {
                    id = Integer.valueOf(matcher.group(1));
                    properties.put(matcher.group(2).trim(), matcher.group(3).trim());
                }
            }
            for (int j = 1; j < lines.length; j++) {
                Matcher matcher = PATTERN.matcher(lines[j]);
                if (matcher.find()) {
                    properties.put(matcher.group(1).trim(), matcher.group(2).trim());
                }
            }
            if (properties.containsKey("source")) {
                SourceOutput item = new SourceOutput(id, properties.get("source"),
                        client.getModule(getNumberValue(properties.get("module"))));
                if (properties.containsKey("state")) {
                    try {
                        item.setState(AbstractAudioDeviceConfig.State.valueOf(properties.get("state")));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("unhandled state {} in source-output item #{}", properties.get("state"), id);
                    }
                }
                if (properties.containsKey("muted")) {
                    item.setMuted("yes".equalsIgnoreCase(properties.get("muted")));
                }
                if (properties.containsKey("volume")) {
                    item.setVolume(Integer.valueOf(parseVolume(properties.get("volume"))));
                }
                if (properties.containsKey("source")) {
                    item.setSource(client.getSource(Integer.valueOf(getNumberValue(properties.get("source")))));
                }
                items.add(item);
            }
        }
        return items;
    }

    /**
     * converts the volume value given by the pulseaudio server
     * to a percentage value. The pulseaudio server sends 2 values for left and right channel volume
     * e.g. 0: 80% 1: 80% which would be converted to 80
     *
     * @param vol
     * @return
     */
    private static int parseVolume(String vol) {
        int volumeTotal = 0;
        int nChannels = 0;
        for (String channel : vol.split(", ")) {
            Matcher matcher = VOLUME_PATTERN.matcher(channel.trim());
            if (matcher.find()) {
                volumeTotal += Integer.valueOf(matcher.group(3));
                nChannels++;
            } else {
                LOGGER.debug("Unable to parse channel volume '{}'", channel);
            }
        }
        if (nChannels > 0) {
            return Math.round(volumeTotal / nChannels);
        }
        return 0;
    }

    /**
     * sometimes the pulseaudio server "forgets" some line feeds which leeds to unparsable number values
     * like 80NextProperty:
     * this is a workaround to get the correct number value in these cases
     *
     * @param raw
     * @return
     */
    private static int getNumberValue(String raw) {
        int id = -1;
        if (raw == null) {
            return 0;
        }
        try {
            id = Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            Matcher matcher = NUMBER_VALUE_PATTERN.matcher(raw.trim());
            if (matcher.find()) {
                id = Integer.valueOf(matcher.group(1));
            }
        }
        return id;
    }
}
