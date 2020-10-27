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
package org.openhab.binding.mpd.internal.protocol;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for representing the status of a Music Player Daemon.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDStatus {

    public enum State {
        PLAY,
        PAUSE,
        STOP
    }

    private final State state;
    private final int volume;
    private final Optional<Float> elapsed;

    public MPDStatus(MPDResponse response) {
        Map<String, String> values = MPDResponseParser.responseToMap(response);
        state = parseState(values.getOrDefault("state", ""));
        volume = MPDResponseParser.parseInteger(values.getOrDefault("volume", "0"), 0);
        elapsed = MPDResponseParser.parseFloat(values.get("elapsed"));
    }

    public State getState() {
        return state;
    }

    public int getVolume() {
        return volume;
    }

    public Optional<Float> getElapsed() {
        return elapsed;
    }

    private State parseState(String value) {
        switch (value) {
            case "play":
                return State.PLAY;
            case "pause":
                return State.PAUSE;
            case "stop":
                return State.STOP;
        }

        return State.STOP;
    }
}
