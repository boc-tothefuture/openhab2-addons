/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link OmnilinkBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Craig - Initial contribution
 */
public class OmnilinkBindingConstants {

    public static final String BINDING_ID = "omnilink";

    // List of all Channel ids

    // zones
    public final static String CHANNEL_ZONE_CONTACT = "contact";
    public final static String CHANNEL_ZONE_CURRENT_CONDITION = "current_condition";
    public final static String CHANNEL_ZONE_LATCHED_ALARM_STATUS = "latched_alarm_status";
    public final static String CHANNEL_ZONE_ARMING_STATUS = "arming_status";
    public final static String CHANNEL_ZONE_BYPASS = "bypass";
    public final static String CHANNEL_ZONE_RESTORE = "restore";

    // areas
    public final static String CHANNEL_AREA_MODE = "mode";
    public final static String CHANNEL_AREA_ACTIVATE_KEYPAD_EMERGENCY = "activate_keypad_emergency";
    public final static String CHANNEL_AREA_ALARM_BURGLARY = "alarm_burglary";
    public final static String CHANNEL_AREA_ALARM_FIRE = "alarm_fire";
    public final static String CHANNEL_AREA_ALARM_GAS = "alarm_gas";
    public final static String CHANNEL_AREA_ALARM_AUXILARY = "alarm_auxiliary";
    public final static String CHANNEL_AREA_ALARM_FREEZE = "alarm_freeze";
    public final static String CHANNEL_AREA_ALARM_WATER = "alarm_water";
    public final static String CHANNEL_AREA_ALARM_DURESS = "alarm_duress";
    public final static String CHANNEL_AREA_ALARM_TEMPERATURE = "alarm_temperature";
    public final static String[] CHANNEL_AREA_ALARMS = { CHANNEL_AREA_ALARM_BURGLARY, CHANNEL_AREA_ALARM_FIRE,
            CHANNEL_AREA_ALARM_GAS, CHANNEL_AREA_ALARM_AUXILARY, CHANNEL_AREA_ALARM_FREEZE, CHANNEL_AREA_ALARM_WATER,
            CHANNEL_AREA_ALARM_DURESS, CHANNEL_AREA_ALARM_TEMPERATURE };
    public final static String CHANNEL_AREA_SECURITY_MODE_DISARM = "disarm";
    public final static String CHANNEL_AREA_SECURITY_MODE_DAY = "day";
    public final static String CHANNEL_AREA_SECURITY_MODE_NIGHT = "night";
    public final static String CHANNEL_AREA_SECURITY_MODE_AWAY = "away";
    public final static String CHANNEL_AREA_SECURITY_MODE_VACATION = "vacation";
    public final static String CHANNEL_AREA_SECURITY_MODE_DAY_INSTANT = "day_instant";
    public final static String CHANNEL_AREA_SECURITY_MODE_NIGHT_DELAYED = "night_delayed";

    // units
    public final static String CHANNEL_UNIT_LEVEL = "level";
    public final static String CHANNEL_FLAG = "value";
    public final static String CHANNEL_ROOM_SWITCH = "switch";
    public final static String CHANNEL_ROOM_ON = "on";
    public final static String CHANNEL_ROOM_OFF = "off";

    public final static String CHANNEL_ROOM_SCENE_A = "scene_a";
    public final static String CHANNEL_ROOM_SCENE_B = "scene_b";
    public final static String CHANNEL_ROOM_SCENE_C = "scene_c";
    public final static String CHANNEL_ROOM_SCENE_D = "scene_d";
    public final static String CHANNEL_ROOM_STATE = "state";

    public final static String CHANNEL_OUTPUT_SWITCH = "switch";

    public final static String CHANNEL_FLAGSWITCH = "switch";
    public final static String CHANNEL_SYSTEMDATE = "sysdate";
    public final static String CHANNEL_EVENT_LOG = "last_log";

    // buttons

    public final static String CHANNEL_BUTTON_PRESS = "press";

    // thermostats
    public final static String CHANNEL_THERMO_FREEZE_ALARM = "freeze_alarm";
    public final static String CHANNEL_THERMO_COMM_FAILURE = "comm_failure";
    public final static String CHANNEL_THERMO_STATUS = "status";
    public final static String CHANNEL_THERMO_TEMP = "temperature";
    public final static String CHANNEL_THERMO_OUTDOOR_TEMP = "outdoor_temperature";
    public final static String CHANNEL_THERMO_HUMIDITY = "humidity";
    public final static String CHANNEL_THERMO_HUMIDIFY_SETPOINT = "humidify_setpoint";
    public final static String CHANNEL_THERMO_DEHUMIDIFY_SETPOINT = "dehumidify_setpoint";
    public final static String CHANNEL_THERMO_SYSTEM_MODE = "system_mode";
    public final static String CHANNEL_THERMO_FAN_MODE = "fan_mode";
    public final static String CHANNEL_THERMO_HOLD_MODE = "hold_mode";
    public final static String CHANNEL_THERMO_COOL_SETPOINT = "cool_setpoint";
    public final static String CHANNEL_THERMO_HEAT_SETPOINT = "heat_setpoint";

    // temp / humidity sensors
    public final static String CHANNEL_AUX_TEMP = "temperature";
    public final static String CHANNEL_AUX_HUMIDITY = "humidity";
    public final static String CHANNEL_AUX_LOW_SETPOINT = "low_setpoint";
    public final static String CHANNEL_AUX_HIGH_SETPOINT = "high_setpoint";

    // consoles
    public final static String CHANNEL_CONSOLE_BEEP = "beep";
    public final static String CHANNEL_CONSOLE_ENABLE_BEEPER = "enable_beeper";

    // audio zones
    public static final String CHANNEL_AUDIO_ZONE_POWER = "zone_power";
    public static final String CHANNEL_AUDIO_ZONE_MUTE = "zone_mute";
    public static final String CHANNEL_AUDIO_ZONE_VOLUME = "zone_volume";
    public static final String CHANNEL_AUDIO_ZONE_SOURCE = "zone_source";
    public static final String CHANNEL_AUDIO_ZONE_CONTROL = "zone_control";

    // audio sources
    public static final String CHANNEL_AUDIO_SOURCE_TEXT1 = "source_text_1";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT2 = "source_text_2";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT3 = "source_text_3";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT4 = "source_text_4";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT5 = "source_text_5";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT6 = "source_text_6";
    public static final String CHANNEL_AUDIO_SOURCE_POLLING = "polling";

    // trigger channels
    public final static String TRIGGER_CHANNEL_BUTTON_ACTIVATED_EVENT = "activated_event";
    public final static String TRIGGER_CHANNEL_PHONE_LINE_EVENT = "phone_line_event";
    public final static String TRIGGER_CHANNEL_AC_POWER_EVENT = "ac_power_event";
    public final static String TRIGGER_CHANNEL_BATTERY_EVENT = "battery_event";
    public final static String TRIGGER_CHANNEL_DCM_EVENT = "dcm_event";
    public final static String TRIGGER_CHANNEL_ENERGY_COST_EVENT = "energy_cost_event";
    public final static String TRIGGER_CHANNEL_CAMERA_TRIGGER_EVENT = "camera_trigger_event";
    public final static String TRIGGER_CHANNEL_ACCESS_CONTROL_READER_EVENT = "access_control_reader_event";
    public final static String TRIGGER_CHANNEL_AREA_ALL_ON_OFF_EVENT = "all_on_off_Event";
    public final static String TRIGGER_CHANNEL_ZONE_STATE_EVENT = "zone_state_Event";

    // thing configuration and properties keys
    public final static String THING_PROPERTIES_NAME = "name";
    public final static String THING_PROPERTIES_NUMBER = "number";
    public final static String THING_PROPERTIES_AREA = "area";
    public final static String THING_PROPERTIES_ROOM = "room";
    public final static String THING_PROPERTIES_AUTO_START = "autostart";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "controller");
    public final static ThingTypeUID THING_TYPE_OMNI_AREA = new ThingTypeUID(BINDING_ID, "area");
    public final static ThingTypeUID THING_TYPE_LUMINA_AREA = new ThingTypeUID(BINDING_ID, "lumina_area");
    public final static ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public final static ThingTypeUID THING_TYPE_UNIT_UPB = new ThingTypeUID(BINDING_ID, "upb");
    public final static ThingTypeUID THING_TYPE_UNIT = new ThingTypeUID(BINDING_ID, "unit");
    public final static ThingTypeUID THING_TYPE_FLAG = new ThingTypeUID(BINDING_ID, "flag");
    public static final ThingTypeUID THING_TYPE_OUTPUT = new ThingTypeUID(BINDING_ID, "output");
    public final static ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public final static ThingTypeUID THING_TYPE_BUTTON = new ThingTypeUID(BINDING_ID, "button");
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public final static ThingTypeUID THING_TYPE_AUDIO_ZONE = new ThingTypeUID(BINDING_ID, "audio_zone");
    public final static ThingTypeUID THING_TYPE_AUDIO_SOURCE = new ThingTypeUID(BINDING_ID, "audio_source");
    public final static ThingTypeUID THING_TYPE_CONSOLE = new ThingTypeUID(BINDING_ID, "console");
    public static final ThingTypeUID THING_TYPE_TEMP_SENSOR = new ThingTypeUID(BINDING_ID, "temp_sensor");
    public static final ThingTypeUID THING_TYPE_HUMIDITY_SENSOR = new ThingTypeUID(BINDING_ID, "humidity_sensor");

    public final static ChannelTypeUID CHANNEL_TYPE_FLAG = new ChannelTypeUID(BINDING_ID, CHANNEL_FLAG);

}
