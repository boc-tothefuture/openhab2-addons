package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 *
 * @author Craig Hamilton
 *
 */
public class ButtonHandler extends AbstractOmnilinkStatusHandler {
    private Logger logger = LoggerFactory.getLogger(ButtonHandler.class);

    public ButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            try {
                int buttonNumber = getThingNumber();
                logger.debug("Executing Button (macro) {}", buttonNumber);
                getOmnilinkBridgeHander().sendOmnilinkCommand(CommandMessage.CMD_BUTTON, 0, buttonNumber);
                updateState(OmnilinkBindingConstants.CHANNEL_BUTTON_PRESS, OnOffType.OFF);
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
        }
    }

    public void buttonActivated() {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(),
                OmnilinkBindingConstants.TRIGGER_CHANNEL_BUTTON_ACTIVATED_EVENT);
        triggerChannel(activateChannel);
    }

    @Override
    protected Optional retrieveStatus() {
        return Optional.empty();
    }

    @Override
    protected void updateChannels(Status t) {
        // No links for buttons
    }
}
