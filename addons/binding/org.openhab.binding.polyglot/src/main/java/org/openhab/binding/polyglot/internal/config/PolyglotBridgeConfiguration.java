/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * The {@link PolyglotBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Brian OConnell - Initial contribution
 */
public class PolyglotBridgeConfiguration {
    public String mqttHostname;

    public int mqttPort;

    public String mqttServer;

    public String mqttUsername;

    public String mqttPassword;

    public String polygotEnvPrefix;

    public ImmutableList<String> getContainerEnv() {
        // Create a map, remove all null and empty values,
        Map<String, String> envMap = Maps.newHashMap();
        envMap.put(polygotEnvPrefix + "MQTT_HOSTNAME", mqttHostname);
        envMap.put(polygotEnvPrefix + "MQTT_PORT", String.valueOf(mqttPort));
        envMap.put(polygotEnvPrefix + "MQTT_SERVER", mqttServer);
        envMap.put(polygotEnvPrefix + "MQTT_USERNAME", mqttUsername);
        envMap.put(polygotEnvPrefix + "MQTT_PASSWORD", mqttPassword);

        envMap = envMap.entrySet().stream()

                .filter(e -> Objects.nonNull(e.getValue()))

                .filter(e -> !e.getValue().isEmpty())

                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> env = envMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        return ImmutableList.copyOf(env);
    }
}
