/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.container;

import java.util.Collections;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;

/**
 *
 * @author Brian OConnell - Initial contribution
 */
class PolygotManaged {

    private static final String POLYGOT_MANAGED_KEY = "polygot-managed";

    private final String polygotManagedValue;

    public final Map<String, String> managedLabel;

    public PolygotManaged(Thing thing) {
        this.polygotManagedValue = thing.getUID().getAsString();
        this.managedLabel = Collections.singletonMap(POLYGOT_MANAGED_KEY, polygotManagedValue);
    }

    String getPolygotManagedKey() {
        return POLYGOT_MANAGED_KEY;
    }

    String getPolygotManagedValue() {
        return polygotManagedValue;
    }

    Map<String, String> getManagedLabel() {
        return managedLabel;
    }

}
