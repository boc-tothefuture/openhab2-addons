/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.container;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.polyglot.internal.PolyglotBindingConstants;
import org.openhab.binding.polyglot.internal.bridge.PolyglotBridge;
import org.openhab.binding.polyglot.internal.config.ContainerConfiguration;
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
import com.spotify.docker.client.exceptions.DockerCertificateException;
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

    private static final int CONTAINER_REFRESH_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(ContainerHandler.class);

    private final ContainerConfiguration config;

    private final PolyglotBridge bridge;

    private Optional<String> containerID = Optional.empty();

    // Logs progress from docker client.
    private final ProgressHandler dockerProgressHandler = new ProgressHandler() {

        @Override
        public void progress(@Nullable ProgressMessage arg0) throws DockerException {
            logger.debug(String.valueOf(arg0));
        }
    };

    // Helper class used to find containers created and managed by the polygot binding.
    private final PolygotManaged polygotManaged = new PolygotManaged(getThing());

    // Shared service used by all container handlers to refresh container info.
    private final static ScheduledExecutorService refreshService = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactory() {

                @Override
                public Thread newThread(@Nullable Runnable runnable) {
                    return new Thread(runnable, "Polygot Refresh Thread");
                }
            });

    // Executor used to attach STDOUT/STDERR to logs.
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(@Nullable Runnable runnable) {
            return new Thread(runnable, getThing().getUID().getAsString() + "-log-thread");
        }
    });

    public ContainerHandler(Thing thing) {
        super(thing);
        this.config = getConfigAs(ContainerConfiguration.class);

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            PolyglotBridge handler = (PolyglotBridge) bridge.getHandler();
            handler = Objects.requireNonNull(handler);
            if (handler != null) {
                this.bridge = handler;
            } else {
                throw new IllegalStateException("ContainerHandler must have a bridge");
            }
        } else {
            throw new IllegalStateException("ContainerHandler must have a bridge");
        }

        // Schedule a refresh of the status of this containers info. Spread out requests over the container refresh
        // interval.
        refreshService.scheduleAtFixedRate(() -> {
            refreshStatus();
        }, hashCode() % CONTAINER_REFRESH_INTERVAL, CONTAINER_REFRESH_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (PolyglotBindingConstants.CONTAINER_STATE.equals(channelUID.getId())) {

            if (command.equals(OnOffType.ON)) {
                // TODO: handle command
            } else if (command.equals(OnOffType.OFF)) {
                // TODO: handle command
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    /**
     * Refresh the status of the container.
     *
     */
    private void refreshStatus() {
        if (containerID.isPresent()) {
            withDockerClient(client -> {

                logger.debug("Container info: {}", client.inspectContainer(containerID.get()));

                // TODO: Fill up some channels with container information.

            });
        } else {
            logger.warn("Cannot refresh status before container starts");
        }
    }

    /**
     * Create the image label for the container which is colon delimited string of the name and tag as defined by user
     * configuration.
     *
     * @return Colon delimited image label
     */
    private String getImageLabel() {

        return String.join(":", asList(config.image, config.tag));
    }

    /**
     * Find any containers created by this handler.
     *
     * @return List of containers created by this handler.
     */
    private List<Container> findContainers() {
        final List<Container> containers = Lists.newArrayList();
        withDockerClient(client -> {
            containers.addAll(client.listContainers(ListContainersParam.allContainers(), ListContainersParam
                    .withLabel(polygotManaged.getPolygotManagedKey(), polygotManaged.getPolygotManagedValue())));
        });
        return containers;
    }

    /**
     * Stop and remove all containers that were created by this handler.
     */
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

    /**
     * Stop and remove any previously created containers.
     * Create a new container with appropriate policies
     *
     * @return ContainerID of newly created container
     */
    private String createContainer() {

        cleanupContainers();

        withDockerClient(client -> {
            HostConfig.Builder hostConfig = HostConfig.builder();
            if (config.runOnStart && config.restart) {
                hostConfig.restartPolicy(RestartPolicy.unlessStopped());
            }

            // Get env list from bridge and our own config.
            ImmutableList<String> env = ImmutableList
                    .copyOf(Iterables.concat(bridge.getContainerEnv(), config.getEnv()));

            ContainerConfig containerConfig = ContainerConfig.builder().image(getImageLabel())
                    .hostConfig(hostConfig.build()).labels(polygotManaged.getManagedLabel()).env(env)
                    .cmd(config.getCommandList()).build();

            final ContainerCreation container = client.createContainer(containerConfig);
            this.containerID = Optional.of(container.id());
        });

        return containerID.get();
    }

    /**
     * Execute the consumer lambda within the context of a newly created and then closed docker client.
     *
     * @param consumer Lamba to execute
     */
    private void withDockerClient(DockerConsumer consumer) {

        try (DockerClient client = bridge.createDockerClient()) {
            consumer.execute(client);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            logger.error("Docker Client Error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /**
     * Initialize the handler, starting container if requested.
     */
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

        if (config.runOnStart) {
            scheduler.execute(() -> {
                startContainer();
                updateStatus(ThingStatus.ONLINE);
            });
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Stop the container and stop the executor that is reading logs from the container.
     */
    @Override
    public void dispose() {
        refreshService.shutdown();
        stopContainer();
        logExecutor.shutdownNow();
    }

    /**
     * Stop the container if has been started.
     */
    private void stopContainer() {
        if (containerID.isPresent()) {
            withDockerClient(client -> {
                performStop(client, containerID.get());
            });
        } else {
            logger.debug("Container ID not yet known.  Unable to stop container.");
        }
    }

    /**
     * Perform the stop operation.
     *
     * @param client      Client to use to execute stop command
     * @param containerID Container ID to stop
     * @throws DockerException      If an error occurs executing the stop command
     * @throws InterruptedException If the thread is interrupted waiting for container to stop
     */
    private void performStop(DockerClient client, String containerID) throws DockerException, InterruptedException {
        logger.debug("Waiting up to {} seconds for container with ID: {} to stop", CONTAINER_STOP_WAIT, containerID);
        client.stopContainer(containerID, CONTAINER_STOP_WAIT);
    }

    /**
     * Pull the container from repository
     * Create the container
     * Start the container
     * Read STDOUT/STDERR from container and attach to logger
     * Refresh the status of the container
     */
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

    /**
     * Attach STDOUT/STDERR to a logger
     */
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
