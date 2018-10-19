/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal;

import static org.openhab.binding.polyglot.internal.PolyglotBindingConstants.CHANNEL_1;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;

/**
 * The {@link PolyglotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian OConnell - Initial contribution
 */
@NonNullByDefault
public class PolyglotHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PolyglotHandler.class);

    @Nullable
    private PolyglotConfiguration config;

    private Optional<DefaultDockerClient.Builder> dockerClientBuilder = Optional.empty();

    public PolyglotHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // TODO Auto-generated method stub
        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // TODO Auto-generated method stub
        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    List<String> getContainerEnv() {
        return config.getContainerEnv();
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(PolyglotConfiguration.class);

        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        try {
            dockerClientBuilder = Optional.of(DefaultDockerClient.fromEnv());
            updateStatus(ThingStatus.ONLINE);
        } catch (DockerCertificateException e) {
            logger.error("Error creating docker client", e);
            updateStatus(ThingStatus.OFFLINE);
        }

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public Optional<DockerClient> getDockerClient() {
        if (dockerClientBuilder.isPresent()) {
            try {
                return Optional.of(DefaultDockerClient.fromEnv().build());
            } catch (DockerCertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
