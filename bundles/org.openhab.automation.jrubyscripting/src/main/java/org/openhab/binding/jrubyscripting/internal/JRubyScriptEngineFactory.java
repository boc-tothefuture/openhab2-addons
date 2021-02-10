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
package org.openhab.binding.jrubyscripting.internal;

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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Ruby.
 * handlers.
 *
 * @author Brian O'Connell - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.automation.jrubyscripting")
public class JRubyScriptEngineFactory extends AbstractScriptEngineFactory {

    private final Logger logger = LoggerFactory.getLogger(JRubyScriptEngineFactory.class);

    private final JRubyScriptEngineConfiguration configuration = new JRubyScriptEngineConfiguration();

    // Filter out the File entry to prevent shadowing the Ruby File class which breaks Ruby in spectacularly
    // difficult ways to debug.
    private final static Set<String> FILTERED_PRESETS = Set.of("File");
    private final static Set<String> INSTANCE_PRESETS = Set.of();
    private final static Set<String> GLOBAL_PRESETS = Set.of("scriptExtension", "automationManager", "ruleRegistry",
            "items", "voice", "rules", "things", "events", "itemRegistry", "ir", "actions", "se", "audio",
            "lifecycleTracker");

    private final org.jruby.embed.jsr223.JRubyEngineFactory factory = new org.jruby.embed.jsr223.JRubyEngineFactory();

    // formatter turned off because it does not appropriately format chained streams
    //@formatter:off
    private final List<String> scriptTypes = Stream
            .of((List<String>) factory.getExtensions(), (List<String>) factory.getMimeTypes())
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableList());
    //@formatter:on

    private static Map.Entry<String, Object> mapInstancePresets(Map.Entry<String, Object> entry) {
        if (INSTANCE_PRESETS.contains(entry.getKey())) {
            return Map.entry("@" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    private static Map.Entry<String, Object> mapGlobalPresets(Map.Entry<String, Object> entry) {
        if (GLOBAL_PRESETS.contains(entry.getKey())) {
            return Map.entry("$" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        configuration.update(config, factory);
    }

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

        // formatter turned off because it does not appropriately format chained streams
        //@formatter:off
        Map<String, Object> filteredScopeValues =
                              scopeValues
                             .entrySet()
                             .stream()
                             .filter(map -> !FILTERED_PRESETS.contains(map.getKey()))
                             .map(JRubyScriptEngineFactory::mapInstancePresets)
                             .map(JRubyScriptEngineFactory::mapGlobalPresets)
                             .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        //@formatter:on

        super.scopeValues(scriptEngine, filteredScopeValues);
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        // return scriptTypes.contains(scriptType) ? configuration.configureScriptEngine(factory) : null;
        return scriptTypes.contains(scriptType) ? configuration.configureRubyEnvironment(factory.getScriptEngine())
                : null;
    }
}
