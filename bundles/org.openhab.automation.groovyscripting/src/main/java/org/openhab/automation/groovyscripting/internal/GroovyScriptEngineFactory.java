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
package org.openhab.automation.groovyscripting.internal;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Groovy.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class GroovyScriptEngineFactory extends AbstractScriptEngineFactory {

    private final org.codehaus.groovy.jsr223.GroovyScriptEngineFactory factory = new org.codehaus.groovy.jsr223.GroovyScriptEngineFactory();

    private final List<String> scriptTypes = (List<String>) Stream.of(factory.getExtensions(), factory.getMimeTypes())
            .flatMap(List::stream) //
            .collect(Collectors.toUnmodifiableList());

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType) ? factory.getScriptEngine() : null;
    }
}
