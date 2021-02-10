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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jruby.embed.jsr223.JRubyEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Processes JRuby Configuration Parameters
 *
 * @author Brian O'Connell - Initial contribution
 */
@NonNullByDefault
public class JRubyScriptEngineConfiguration {

    private final static Map<String, OptionalConfigurationElement> CONFIGURATION_PARAMETERS = Map.ofEntries(
            Map.entry("local_context",
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.SYSTEM_PROPERTY)
                            .mappedTo("org.jruby.embed.localcontext.scope").defaultValue("threadsafe").build()),

            Map.entry("local_variable",
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.SYSTEM_PROPERTY)
                            .mappedTo("org.jruby.embed.localvariable.behavior").defaultValue("transient").build()),

            Map.entry("gem_home",
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT)
                            .mappedTo("GEM_HOME").build()),

            Map.entry("gems", new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.GEM).build()));

    private final static Map<OptionalConfigurationElement.Type, List<OptionalConfigurationElement>> CONFIGURATION_TYPE_MAP = CONFIGURATION_PARAMETERS
            .values().stream().collect(Collectors.groupingBy(v -> v.type));

    private final Logger logger = LoggerFactory.getLogger(JRubyScriptEngineConfiguration.class);

    void update(Map<String, Object> config, JRubyEngineFactory factory) {
        logger.trace("JRuby Script Engine Configuration: {}", config);
        config.forEach(this::processConfigValue);
        configureScriptEngine(factory);
    }

    private void processConfigValue(String key, Object value) {
        OptionalConfigurationElement configurationElement = CONFIGURATION_PARAMETERS.get(key);
        if (configurationElement != null) {
            configurationElement.setValue(value.toString());
        } else {
            logger.debug("Ignoring unexpected configuration key: {}", key);
        }
    }

    ScriptEngine configureScriptEngine(JRubyEngineFactory factory) {

        configureSystemProperties(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.SYSTEM_PROPERTY,
                Collections.EMPTY_LIST));
        ScriptEngine engine = factory.getScriptEngine();

        configureRubyEnvironment(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                Collections.EMPTY_LIST), engine);
        configureGems(
                CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.GEM, Collections.EMPTY_LIST),
                engine);

        return engine;
    }

    private void configureGems(List<OptionalConfigurationElement> optionalConfigurationElements, ScriptEngine engine) {
        for (OptionalConfigurationElement configElement : optionalConfigurationElements) {
            if (configElement.getValue().isPresent()) {

                String[] gems = configElement.getValue().get().split(",");
                for (String gem : gems) {
                    gem = gem.trim();
                    String gemCommand;
                    if (gem.contains("=")) {
                        String[] gemParts = gem.split("=");
                        gem = gemParts[0];
                        String version = gemParts[1];
                        gemCommand = "cmd.handle_options ['--no-document', '" + gem + "', '-v', '" + version + "']";
                    } else {
                        gemCommand = "cmd.handle_options ['--no-document', '" + gem + "']";
                    }

                    // formatter turned off because it does not appropriately format multi-line strings
                    //@formatter:off
                    String gemInstallCode =
                              "require 'rubygems/commands/install_command'\n"
                            + "cmd = Gem::Commands::InstallCommand.new\n"
                            +  gemCommand + "\n"
                            + "cmd.execute";
                    //@formatter:on

                    try {
                        logger.debug("Installing Gem: {} ", gem);
                        logger.trace("Gem install code:\n{}\n", gemInstallCode);
                        engine.eval(gemInstallCode);
                    } catch (ScriptException e) {
                        logger.error("Error installing Gem", e);
                    }
                }
            } else {
                logger.debug("Ruby gem property has no value");
            }
        }
    }

    public ScriptEngine configureRubyEnvironment(ScriptEngine scriptEngine) {
        configureRubyEnvironment(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                Collections.EMPTY_LIST), scriptEngine);
        return scriptEngine;
    }

    private void configureRubyEnvironment(List<OptionalConfigurationElement> optionalConfigurationElements,
            ScriptEngine engine) {
        for (OptionalConfigurationElement configElement : optionalConfigurationElements) {
            String environmentProperty = configElement.mappedTo.get();
            if (configElement.getValue().isPresent()) {
                String environmentSetting = "ENV['" + environmentProperty + "']='" + configElement.getValue().get()
                        + "'";
                try {
                    logger.trace("Setting Ruby environment with code: {} ", environmentSetting);
                    engine.eval(environmentSetting);
                } catch (ScriptException e) {
                    logger.error("Error setting ruby environment", e);
                }
            } else {
                logger.debug("Ruby environment property ({}) has no value", environmentProperty);
            }
        }
    }

    private void configureSystemProperties(List<OptionalConfigurationElement> optionalConfigurationElements) {
        for (OptionalConfigurationElement configElement : optionalConfigurationElements) {
            String systemProperty = configElement.mappedTo.get();
            if (configElement.getValue().isPresent()) {
                String propertyValue = configElement.getValue().get();
                logger.trace("Setting system property ({}) to ({})", systemProperty, propertyValue);
                System.setProperty(systemProperty, propertyValue);
            } else {
                logger.warn("System property ({}) has no value", systemProperty);
            }
        }
    }

    /**
     * Inner static companion class for configuration elements
     */
    private static class OptionalConfigurationElement {

        private final Optional<String> defaultValue;
        private final Optional<String> mappedTo;
        private final Type type;
        private Optional<String> value;

        private OptionalConfigurationElement(Type type, @Nullable String mappedTo, @Nullable String defaultValue) {
            this.type = type;
            this.defaultValue = Optional.ofNullable(defaultValue);
            this.mappedTo = Optional.ofNullable(mappedTo);
            value = Optional.empty();
        }

        private Optional<String> getValue() {
            return value.or(() -> defaultValue);
        }

        private void setValue(String value) {
            this.value = Optional.of(value);
        }

        private Optional<String> mappedTo() {
            return mappedTo;
        }

        private enum Type {
            SYSTEM_PROPERTY,
            RUBY_ENVIRONMENT,
            GEM
        }

        private static class Builder {
            private final Type type;
            private @Nullable String defaultValue = null;
            private @Nullable String mappedTo = null;

            private Builder(Type type) {
                this.type = type;
            }

            private Builder mappedTo(String mappedTo) {
                this.mappedTo = mappedTo;
                return this;
            }

            private Builder defaultValue(String value) {
                this.defaultValue = value;
                return this;
            }

            private OptionalConfigurationElement build() {
                return new OptionalConfigurationElement(type, mappedTo, defaultValue);
            }
        }
    }
}
