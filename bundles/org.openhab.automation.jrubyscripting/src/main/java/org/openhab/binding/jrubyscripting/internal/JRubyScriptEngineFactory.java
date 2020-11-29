/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.osgi.service.component.annotations.Component;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Ruby.
 * handlers.
 *
 * @author Brian O'Connell - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class)
public class JRubyScriptEngineFactory extends AbstractScriptEngineFactory {

    static {
        System.setProperty("org.jruby.embed.localcontext.scope", "threadsafe");
        System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
    }

    private final static Set<String> FILTERED_PRESETS = Set.of("File");
    private final static Set<String> INSTANCE_PRESETS = Set.of();
    private final static Set<String> GLOBAL_PRESETS = Set.of("scriptExtension", "automationManager", "ruleRegistry",
            "items", "voice", "rules", "things", "events", "itemRegistry", "ir", "actions", "se", "audio",
            "lifecycleTracker");

    private final org.jruby.embed.jsr223.JRubyEngineFactory factory = new org.jruby.embed.jsr223.JRubyEngineFactory();

    private final List<String> scriptTypes = Stream
            .of((List<String>) factory.getExtensions(), (List<String>) factory.getMimeTypes()).flatMap(List::stream)
            .collect(Collectors.toUnmodifiableList());

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

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // Filter out the File entry to prevent shadowing the Ruby File class which breaks ruby in spectacularly
        // difficult was to debug.
        // formatter turned off because it does not appropriately format chained streams
        //@formatter:off
        Map<String, Object> filteredScopeValues = scopeValues.entrySet().stream()
                            .filter(map -> !FILTERED_PRESETS.contains(map.getKey()))
                            .map(JRubyScriptEngineFactory::mapInstancePresets)
                            .map(JRubyScriptEngineFactory::mapGlobalPresets)
                            .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        //@formatter:on

        /*
         * System.out.println("Filtered!");
         * for (Map.Entry<String, Object> entry : filteredScopeValues.entrySet()) {
         * System.out.println(entry.getKey() + ":" + entry.getValue().toString());
         * }
         * System.out.println("Filtered!");
         */

        super.scopeValues(scriptEngine, filteredScopeValues);
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        /*
         * try {
         * factory.getScriptEngine().eval(new FileReader(
         * "/Users/boc@us.ibm.com/personal/openhab-3.0.0.M2/conf/automation/jsr223/ruby/hello.rb"));
         * } catch (Exception e) {
         * System.out.println(e);
         * }
         */

        return scriptTypes.contains(scriptType) ? factory.getScriptEngine() : null;
    }
}
