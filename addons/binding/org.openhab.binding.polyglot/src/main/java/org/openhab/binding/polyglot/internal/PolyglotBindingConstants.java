/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PolyglotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian OConnell - Initial contribution
 */
@NonNullByDefault
public class PolyglotBindingConstants {

    private static final String BINDING_ID = "polyglot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "containers");
    public static final ThingTypeUID THING_TYPE_CONTAINER = new ThingTypeUID(BINDING_ID, "container");

    // List of all Channel ids
    public static final String CONTAINER_STATE = "container_state";
}
