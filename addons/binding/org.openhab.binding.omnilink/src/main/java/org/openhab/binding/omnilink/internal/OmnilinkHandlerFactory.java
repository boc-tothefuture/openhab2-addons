/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.internal;

import static org.openhab.binding.omnilink.OmnilinkBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.omnilink.discovery.OmnilinkDiscoveryService;
import org.openhab.binding.omnilink.handler.AreaHandler;
import org.openhab.binding.omnilink.handler.AudioSourceHandler;
import org.openhab.binding.omnilink.handler.AudioZoneHandler;
import org.openhab.binding.omnilink.handler.ButtonHandler;
import org.openhab.binding.omnilink.handler.FlagHandler;
import org.openhab.binding.omnilink.handler.HumiditySensorHandler;
import org.openhab.binding.omnilink.handler.OmnilinkBridgeHandler;
import org.openhab.binding.omnilink.handler.OutputHandler;
import org.openhab.binding.omnilink.handler.RoomHandler;
import org.openhab.binding.omnilink.handler.TempSensorHandler;
import org.openhab.binding.omnilink.handler.ThermostatHandler;
import org.openhab.binding.omnilink.handler.UpbUnitHandler;
import org.openhab.binding.omnilink.handler.ZoneHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link OmnilinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Craig - Initial contribution
 */
public class OmnilinkHandlerFactory extends BaseThingHandlerFactory {

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        // if the omnilink bridge, let's fix up discovery
        super.removeHandler(thingHandler);
        if (thingHandler instanceof OmnilinkBridgeHandler) {
            ServiceRegistration<?> discovery = discoveryServiceRegistrations.get(thingHandler.getThing().getUID());
            logger.debug("unRegistering omnilink discovery: {} ", discovery);
            discovery.unregister();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(OmnilinkHandlerFactory.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_OMNI_AREA,
            THING_TYPE_ZONE, THING_TYPE_BRIDGE, THING_TYPE_FLAG, THING_TYPE_OUTPUT, THING_TYPE_ROOM, THING_TYPE_BUTTON,
            THING_TYPE_UNIT_UPB, THING_TYPE_THERMOSTAT, THING_TYPE_CONSOLE, THING_TYPE_AUDIO_ZONE,
            THING_TYPE_AUDIO_SOURCE, THING_TYPE_TEMP_SENSOR, THING_TYPE_HUMIDITY_SENSOR);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<ThingUID, ServiceRegistration<?>>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_UNIT_UPB)) {
            return new UpbUnitHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            OmnilinkBridgeHandler handler = new OmnilinkBridgeHandler((Bridge) thing);
            registerOmnilnkBridgeDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new ZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OMNI_AREA)) {
            return new AreaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FLAG)) {
            return new FlagHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OUTPUT)) {
            return new OutputHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROOM)) {
            return new RoomHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BUTTON)) {
            return new ButtonHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new ThermostatHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AUDIO_ZONE)) {
            return new AudioZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AUDIO_SOURCE)) {
            return new AudioSourceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMP_SENSOR)) {
            return new TempSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HUMIDITY_SENSOR)) {
            return new HumiditySensorHandler(thing);
        } else {
            logger.warn("Unsupported Thing - {} not added to handler", thing);
            return null;
        }

    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerOmnilnkBridgeDiscoveryService(OmnilinkBridgeHandler bridgeHandler) {
        OmnilinkDiscoveryService discoveryService = new OmnilinkDiscoveryService(bridgeHandler);

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);

        logger.debug(
                "registerOmnilinkBridgeDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                bridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

}
