/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal;

import static java.util.Arrays.asList;
import static org.openhab.binding.polyglot.internal.PolyglotBindingConstants.CHANNEL_1;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.AttachParameter;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.RestartPolicy;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.shaded.com.google.common.collect.Iterables;

/**
 * The {@link ContainerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian OConnell - Initial contribution
 */
@NonNullByDefault
public class ContainerHandler extends BaseThingHandler {

    private static final int CONTAINER_STOP_WAIT = 60;

    private final Logger logger = LoggerFactory.getLogger(ContainerHandler.class);

    private Optional<String> containerID = Optional.empty();

    private final ProgressHandler dockerProgressHandler = new ProgressHandler() {

        @Override
        public void progress(@Nullable ProgressMessage arg0) throws DockerException {
            logger.debug(String.valueOf(arg0));
        }
    };

    private final PolygotManaged polygotManaged = new PolygotManaged(getThing());

    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(@Nullable Runnable runnable) {
            return new Thread(runnable, getThing().getUID().getAsString() + "-log-thread");
        }
    });

    @Nullable
    private ContainerConfiguration config = null;

    public ContainerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                refreshStatus();
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private void refreshStatus() {
        if (containerID.isPresent()) {
            withDockerClient(client -> {
                logger.debug("Container info: {}", client.inspectContainer(containerID.get()));
                client.inspectContainer(containerID.get()).state().finishedAt();
            });
        } else {
            logger.warn("Cannot refresh status before container starts");
        }
    }

    private String getImageLabel() {
        return String.join(":", asList(config.image, config.tag));

    }

    private List<Container> findContainers() {
        final List<Container> containers = Lists.newArrayList();
        withDockerClient(client -> {
            containers.addAll(client.listContainers(ListContainersParam.allContainers(), ListContainersParam
                    .withLabel(polygotManaged.getPolygotManagedKey(), polygotManaged.getPolygotManagedValue())));
        });
        return containers;
    }

    private void cleanupContainers() {
        List<Container> containers = findContainers();
        // If more than one container exists, we wouldn't know which one to manage. Destroy them all.
        if (containers.size() > 0) {

            logger.debug("{} managed containers found, stopping and removing all.", containers.size());
            withDockerClient(client -> {
                for (Container container : containers) {
                    logger.debug("Stopping and removing container with id {}", container.id());
                    performStop(client, container.id());
                    client.removeContainer(container.id());
                }
            });
        }
    }

    private String createContainer() {
        cleanupContainers();

        withDockerClient(client -> {
            HostConfig.Builder hostConfig = HostConfig.builder();
            if (config.runOnStart && config.restart) {
                hostConfig.restartPolicy(RestartPolicy.unlessStopped());
            }

            // Get env list from bridge and our own config.
            ImmutableList<String> env = ImmutableList.copyOf(
                    Iterables.concat(((PolyglotHandler) getBridge().getHandler()).getContainerEnv(), config.getEnv()));

            ContainerConfig containerConfig = ContainerConfig.builder().image(getImageLabel())
                    .hostConfig(hostConfig.build()).labels(polygotManaged.getManagedLabel()).env(env)
                    .cmd(config.getCommandList()).build();

            final ContainerCreation container = client.createContainer(containerConfig);
            this.containerID = Optional.of(container.id());
        });

        return containerID.get();
    }

    private void withDockerClient(DockerConsumer consumer) {
        PolyglotHandler bridge = (PolyglotHandler) getBridge().getHandler();
        Optional<DockerClient> optionalClient = bridge.getDockerClient();
        if (optionalClient.isPresent()) {
            DockerClient client = optionalClient.get();
            try {
                consumer.execute(client);
            } catch (DockerException | InterruptedException e) {
                logger.error("Docker Client Error", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } finally {
                client.close();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polygot does not contain a docker client");
        }
    }

    @Override
    public void initialize() {

        this.config = getConfigAs(ContainerConfiguration.class);

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

        if (config.runOnStart) {
            scheduler.execute(() -> {
                startContainer();
            });
        }
    }

    @Override
    public void dispose() {
        stopContainer();
        logExecutor.shutdownNow();
    }

    private void stopContainer() {
        if (containerID.isPresent()) {
            withDockerClient(client -> {
                performStop(client, containerID.get());
            });
        } else {
            logger.debug("Container ID not yet known.  Unable to stop container.");
        }
    }

    private void performStop(DockerClient client, String containerID) throws DockerException, InterruptedException {
        logger.debug("Waiting up to {} seconds for container with ID: {} to stop", CONTAINER_STOP_WAIT, containerID);
        client.stopContainer(containerID, CONTAINER_STOP_WAIT);
    }

    private void startContainer() {

        withDockerClient(client -> {
            client.pull(getImageLabel(), dockerProgressHandler);
        });

        String containerID = createContainer();
        withDockerClient(client -> {
            client.startContainer(containerID);
            logger.debug("Started container {} with ID {}", getImageLabel(), containerID);
            updateStatus(ThingStatus.ONLINE);
        });
        ;
        attachContainer();
        refreshStatus();
    }

    private void attachContainer() {
        logExecutor.execute(() -> {
            withDockerClient(client -> {
                try {
                    LogStream stream = client.attachContainer(containerID.get(), AttachParameter.STDOUT,
                            AttachParameter.STDIN, AttachParameter.STREAM);
                    stream.attach(new LoggingOutputStream(Level.INFO, getThing()),
                            new LoggingOutputStream(Level.ERROR, getThing()));
                } catch (IOException e) {
                    logger.error("Error reading streams from docker client", e);
                } catch (InterruptedException e) {
                    logger.debug(Thread.currentThread().getName() + " interrupted.");
                }

            });
        });
    }
}
