/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.container;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

/**
 *
 * @author Brian OConnell - Initial contribution
 */
@FunctionalInterface
public interface DockerConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void execute(DockerClient client) throws DockerException, InterruptedException;

}
