/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

/**
 * The {@link ContainerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Brian OConnell - Initial contribution
 */
public class ContainerConfiguration {

    public String image;

    public String tag;

    public boolean runOnStart;

    public boolean restart;

    public boolean skipPull;

    public String cmd;

    public String env;

    public String mqttClientID;

    public String logRegex;

    public List<String> getCommandList() {
        Gson gson = new Gson();
        String[] commands = gson.fromJson(cmd, String[].class);
        return Arrays.asList(commands);
    }

    @SuppressWarnings("unchecked")
    public List<String> getEnv(String polygotEnvPrefix, String thingUUID) {
        Gson gson = new Gson();

        Map<String, String> envMap = Maps.newHashMap();
        envMap = gson.fromJson(env, envMap.getClass());

        if (mqttClientID != null) {
            envMap.put(polygotEnvPrefix + "MQTT_CLIENT_ID", mqttClientID);
        } else {
            envMap.put(polygotEnvPrefix + "MQTT_CLIENT_ID", thingUUID);
        }

        List<String> env = envMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        return ImmutableList.copyOf(env);
    }

}
