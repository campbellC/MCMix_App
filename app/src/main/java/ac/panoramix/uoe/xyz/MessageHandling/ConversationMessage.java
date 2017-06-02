package ac.panoramix.uoe.xyz.MessageHandling;

import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class ConversationMessage {
    private byte[] msg;
    private byte[] dead_drop;

    public ConversationMessage(String message, byte[] dead_drop){
        // If the message is too long it is silently truncated, splitting into
        // multiple messages should be done before passing to Conversation Message

        //TODO: on the backend are we expecting null terminated string and if so
        // TODO: do we need to do this before sending or on receiving?

        assert dead_drop.length == XYZConstants.DEAD_DROP_LENGTH;

        if(message.length() > XYZConstants.MESSAGE_LENGTH){
            message = message.substring(0, XYZConstants.MESSAGE_LENGTH);
        }
        msg = message.getBytes();
        this.dead_drop = dead_drop;
    }

    public byte[] getPayload(){
        byte[] payload = new byte[dead_drop.length + msg.length];
        System.arraycopy(dead_drop, 0, payload, 0, dead_drop.length);
        System.arraycopy(msg, 0, payload, dead_drop.length, msg.length);
        return payload;
    }
}
