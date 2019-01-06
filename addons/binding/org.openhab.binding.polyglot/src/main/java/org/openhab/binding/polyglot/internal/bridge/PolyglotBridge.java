/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.bridge;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.polyglot.internal.config.PolyglotBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * The {@link PolyglotBridge} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian OConnell - Initial contribution
 */
@NonNullByDefault
public class PolyglotBridge extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PolyglotBridge.class);
    private final PolyglotBridgeConfiguration config;

    public PolyglotBridge(Bridge bridge) {
        super(bridge);
        this.config = getConfigAs(PolyglotBridgeConfiguration.class);
    }

    public List<String> getContainerEnv() {
        return config.getContainerEnv();
    }

    public String getPolygotEnvPrefix() {
        return config.polygotEnvPrefix;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void initialize() {
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try (DockerClient client = createDockerClient()) {
                logger.debug("Docker Client Info: " + String.valueOf(client.info()));
                updateStatus(ThingStatus.ONLINE);
            } catch (DockerException | InterruptedException | DockerCertificateException e) {
                logger.error("Error creating docker client", e);
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public DockerClient createDockerClient() throws DockerCertificateException {
        return DefaultDockerClient.fromEnv().build();
    }

    @Override
    public void handleCommand(ChannelUID uid, Command command) {
        logger.error("Polygot bridge does not respond to commands.");
    }

}
