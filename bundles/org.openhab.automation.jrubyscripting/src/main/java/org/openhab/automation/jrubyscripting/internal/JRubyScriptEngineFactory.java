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
package org.openhab.automation.jrubyscripting.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Ruby.
 *
 * @author Brian O'Connell - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.automation.jrubyscripting")
@ConfigurableService(category = "automation", label = "JRuby Scripting", description_uri = "automation:jruby")
public class JRubyScriptEngineFactory extends AbstractScriptEngineFactory {

    private final JRubyScriptEngineConfiguration configuration = new JRubyScriptEngineConfiguration();

    // Filter out the File entry to prevent shadowing the Ruby File class which breaks Ruby in spectacularly
    // difficult ways to debug.
    private static final Set<String> FILTERED_PRESETS = Set.of("File");
    private static final Set<String> INSTANCE_PRESETS = Set.of();
    private static final Set<String> GLOBAL_PRESETS = Set.of("scriptExtension", "automationManager", "ruleRegistry",
            "items", "voice", "rules", "things", "events", "itemRegistry", "ir", "actions", "se", "audio",
            "lifecycleTracker");

    private final javax.script.ScriptEngineFactory factory = new org.jruby.embed.jsr223.JRubyEngineFactory();

    private final List<String> scriptTypes = Stream
            .concat(factory.getExtensions().stream(), factory.getMimeTypes().stream())
            .collect(Collectors.toUnmodifiableList());

    // Adds @ in front of a set of variables so that Ruby recogonizes them as instance variables
    private static Map.Entry<String, Object> mapInstancePresets(Map.Entry<String, Object> entry) {
        if (INSTANCE_PRESETS.contains(entry.getKey())) {
            return Map.entry("@" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    // Adds $ in front of a set of variables so that Ruby recogonizes them as global variables
    private static Map.Entry<String, Object> mapGlobalPresets(Map.Entry<String, Object> entry) {
        if (GLOBAL_PRESETS.contains(entry.getKey())) {
            return Map.entry("$" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    // The activate call activates the automation and sets its configuration
    @Activate
    protected void activate(Map<String, Object> config) {
        configuration.update(config, factory);
    }

    // The modified call updates configuration for the automation
    @Modified
    protected void modified(Map<String, Object> config) {
        configuration.update(config, factory);
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // Empty comments prevent the formatter from breaking up the correct streams chaining
        Map<String, Object> filteredScopeValues = //
                scopeValues //
                        .entrySet() //
                        .stream() //
                        .filter(map -> !FILTERED_PRESETS.contains(map.getKey())) //
                        .map(JRubyScriptEngineFactory::mapInstancePresets) //
                        .map(JRubyScriptEngineFactory::mapGlobalPresets) //
                        .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())); //

        super.scopeValues(scriptEngine, filteredScopeValues);
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType) ? configuration.configureRubyEnvironment(factory.getScriptEngine())
                : null;
    }
}
