package ac.panoramix.uoe.xyz.MessageHandling;

import java.nio.charset.StandardCharsets;

import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 *
 *
 */

public class ConversationMessage {

    private String message;

    public ConversationMessage(String message) {
        assert message.length() < XYZConstants.MESSAGE_LENGTH;
        this.message = message;
    }

    public byte[] getBytes(){
        return message.getBytes(StandardCharsets.UTF_8);
    }

}
