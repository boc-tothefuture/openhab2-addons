package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class OutputHandler extends AbstractOmnilinkStatusHandler<UnitStatus> implements UnitHandler {

    public OutputHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(OutputHandler.class);

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        super.handleUpdate(channelUID, newState);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel:{}, command:{}", channelUID, command);

        int outputID = getThingNumber();
        if (command instanceof OnOffType) {
            logger.debug("updating omnilink output change: {}, command: {}", channelUID, command);
            try {
                OmniLinkCmd omniLinkCmd = OnOffType.ON.equals(command) ? OmniLinkCmd.CMD_UNIT_ON
                        : OmniLinkCmd.CMD_UNIT_OFF;
                getOmnilinkBridgeHander().sendOmnilinkCommand(omniLinkCmd.getNumber(), 0, outputID);
            } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                    | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
        } else {
            logger.warn("Must handle command: {}", command);
        }
    }

    @Override
    public void updateChannels(UnitStatus unitStatus) {
        logger.debug("Updating output status {}", unitStatus);
        updateState(OmnilinkBindingConstants.CHANNEL_OUTPUT_SWITCH,
                unitStatus.getStatus() == 0 ? OnOffType.OFF : OnOffType.ON);

    }

    @Override
    protected Optional<UnitStatus> retrieveStatus() {
        try {
            int outputID = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_UNIT, outputID,
                    outputID, false);
            return Optional.of((UnitStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        updateChannels(unitStatus);

    }
}
