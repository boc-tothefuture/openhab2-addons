package org.openhab.binding.polyglot.internal;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

@FunctionalInterface
public interface DockerConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void execute(DockerClient client) throws DockerException, InterruptedException;

}
