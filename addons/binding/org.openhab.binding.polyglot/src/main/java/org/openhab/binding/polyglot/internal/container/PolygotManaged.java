package org.openhab.binding.polyglot.internal.container;

import java.util.Collections;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;

class PolygotManaged {

    private final static String POLYGOT_MANAGED_KEY = "polygot-managed";

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
